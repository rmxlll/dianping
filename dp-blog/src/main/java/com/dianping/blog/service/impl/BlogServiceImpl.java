package com.dianping.blog.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.dianping.api.client.FollowClient;
import com.dianping.api.client.UserClient;
import com.dianping.api.vo.FollowVO;
import com.dianping.api.vo.UserVO;
import com.dianping.blog.domain.po.Blog;
import com.dianping.blog.exception.BlogNotExistException;
import com.dianping.blog.exception.BlogSaveException;
import com.dianping.blog.mapper.BlogMapper;
import com.dianping.blog.service.IBlogService;
import com.dianping.common.UserDTO;
import com.dianping.common.utils.Result;
import com.dianping.common.utils.SystemConstants;
import com.dianping.common.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.dianping.common.utils.RedisConstants.BLOG_LIKED_KEY;



@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private FollowClient followClient;
    @Override
    public List<Blog> queryHotBlog(Integer current) {
        // 根据用户查询
        Page<Blog> page = this.query()
                .orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        // 查询用户
        records.forEach(blog ->{
            Long userId = blog.getUserId();
            UserVO user = userClient.getUser(userId);
            blog.setName(user.getNickName());
            blog.setIcon(user.getIcon());
        });
        return records;
    }

    @Override
    public Blog queryBlogById(Long id) {
        Blog blog = this.getById(id);
        if(Objects.isNull(blog)){
            throw new BlogNotExistException("笔记不存在");
        }
        // 插入用户信息
        queryBlogUser(blog);
        // 插入当前用户信息
        isBlogLiked(blog);
        return blog;
    }
    private void queryBlogUser(Blog blog) {
        Long userId = blog.getUserId();
        UserVO user = userClient.getUser(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
    }

    private void isBlogLiked(Blog blog) {
        //1.获取登录用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            //用户未登录，无需查询是否登录
            return;
        }
        Long userId = user.getId();
        //2.判断当前登录用户是否已经点赞
        String key = "blog:liked:" + blog.getId();
        Double isMember = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        blog.setIsLike(isMember != null);
    }


    @Override
    public Boolean likeBlog(Long id) {
        // 1.判断当前用户是否点赞
        Long userId = UserHolder.getUser().getId();
        // 博客Redis的Key
        String key = BLOG_LIKED_KEY+id;
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(key,userId.toString());
        boolean result;
        if(BooleanUtil.isFalse(isMember)){
            result = this.update(new LambdaUpdateWrapper<Blog>()
                    .eq(Blog::getId, id)
                    .setSql("liked = liked + 1"));
            if (result) {
                // 数据库更新成功，更新缓存  sadd key value
                stringRedisTemplate.opsForZSet().add(key, userId.toString(),System.currentTimeMillis());
            }
        } else {
            result =this.update(new LambdaUpdateWrapper<Blog>()
                    .eq(Blog::getId, id)
                    .setSql("liked = liked - 1"));
            if(result){
                stringRedisTemplate.opsForZSet().remove(key,userId.toString());
            }
        }
        return true;
    }

    @Override
    public List<UserDTO> queryBlogLikes(Long id) {
        Long userId = UserHolder.getUser().getId();
        String key = BLOG_LIKED_KEY + id;
        Set<String> top5 = stringRedisTemplate.opsForZSet().range(key, 0, 4);
        if(top5 == null||top5.isEmpty()){
            return Collections.emptyList();
        }
        List<Long> ids = top5.stream().map(Long::valueOf).collect(Collectors.toList());
        String idStr = StrUtil.join(",", ids);
        List<UserDTO> userDTOList = userClient.list(new LambdaQueryWrapper<UserVO>().
                in(UserVO::getId,ids).last("order by field (id," + idStr + ")")).
                stream().map(user -> BeanUtil.copyProperties(user, UserDTO.class)).collect(Collectors.toList());

        return userDTOList;
    }

    @Override
    public Boolean saveBlog(Blog blog) {
        Long userId = UserHolder.getUser().getId();
        blog.setUserId(userId);
        boolean isSuccess = this.save(blog);
        if(!isSuccess){
            throw new BlogSaveException("保存博客失败");
        }
        List<FollowVO> follows = followClient.getFollowsByUserId(userId);
        for(FollowVO follow: follows){
            Long followUserId = follow.getUserId();
            String key = "feed:"+followUserId;
            stringRedisTemplate.opsForZSet().add(key,blog.getId().toString(),System.currentTimeMillis());
        }
        // 返回id
        return true;
    }

    @Override
    public List<UserDTO> queryBlogOfFollow(Long max, Integer offset) {
        return null;
    }

    @Override
    public Map<String, Object> queryMyBlog(Integer current) {
        UserDTO user = UserHolder.getUser();
        // 则从数据库中查询数据
        Page<Blog> page = this.query().eq("user_id", user.getId())
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        List<Blog> records = page.getRecords();
        long pageTotal = page.getPages();
        // 将查询到的数据存入Map中
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("records", records);
        resultData.put("totalPage", pageTotal);
        return resultData;
    }
}
