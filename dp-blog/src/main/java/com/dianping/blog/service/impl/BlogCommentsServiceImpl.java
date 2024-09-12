package com.dianping.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.dianping.api.client.UserClient;
import com.dianping.api.vo.UserVO;
import com.dianping.blog.domain.po.Blog;
import com.dianping.blog.domain.po.BlogComments;
import com.dianping.blog.exception.CommentSaveException;
import com.dianping.blog.mapper.BlogCommentsMapper;
import com.dianping.blog.service.IBlogCommentsService;
import com.dianping.blog.service.IBlogService;
import com.dianping.common.utils.Result;
import com.dianping.common.utils.UserHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments> implements IBlogCommentsService {

    @Resource
    private BlogCommentsMapper commentsMapper;

    @Resource
    private IBlogService blogService;
    @Resource
    private UserClient userClient;
    @Override
    public List<BlogComments> findCommentDetail(Long blogId) {
        return commentsMapper.findCommentDetail(blogId);
    }

    @Override
    public Boolean saveComments(BlogComments comment) {
        if(comment.getId()!=null){
            // 1.给评论设置用户信息
            Long userId = UserHolder.getUser().getId();
            UserVO user = userClient.getUser(userId);
            comment.setUserId(user.getId());
            comment.setIcon(user.getIcon());
            comment.setNickName(user.getNickName());

            if(comment.getAnswerId()!=null){
                BlogComments pComment = this.getById(comment.getAnswerId());
                if(pComment.getParentId() != null && pComment.getParentId()!=0L){
                    comment.setParentId(pComment.getParentId());
                } else {
                    comment.setParentId(0L);
                }
            }
        }
        Long blogId = comment.getBlogId();
        Blog blog = blogService.query().eq("id", blogId).one();
        blog.setIsRead(false);
        boolean saveOrUpdate = blogService.saveOrUpdate(blog);
        boolean update = this.saveOrUpdate(comment);
        if (update && saveOrUpdate) {
            boolean isSuccess = blogService.update().setSql("comments=comments+1")
                    .eq("id", comment.getBlogId()).update();
            if (!isSuccess) {
                throw new CommentSaveException("保存失败");
            }
        }

        return true;
    }

    @Override
    public List<BlogComments> findTree(Long blogId) {
        // 1.查找所有评论和回复
        List<BlogComments> articleComment= this.findCommentDetail(blogId);
        // 2.查找所有评论不包括回复
        List<BlogComments> originList = articleComment.stream().filter(comment -> comment.getParentId()==0).collect(Collectors.toList());
        for (BlogComments origin:originList){
        //     找到每个评论的回复
            List<BlogComments> comments = articleComment.stream().filter(comment -> origin.getId().equals(comment.getParentId())).collect(Collectors.toList());
            comments.forEach(comment ->{
            //     找到当前评论的父级，给自己的评论设置父级用户id和昵称
                Optional<BlogComments> pComment = articleComment.stream().filter(c1 -> c1.getId().equals(comment.getAnswerId())).findFirst();
                pComment.ifPresent(v -> {
                    comment.setAUserID(v.getAUserID());
                    comment.setANickname(v.getANickname());
                });
            });
            origin.setChildren(comments);
        }
        return originList;
    }

}
