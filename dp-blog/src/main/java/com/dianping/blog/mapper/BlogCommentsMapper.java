package com.dianping.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dianping.blog.domain.po.BlogComments;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


public interface BlogCommentsMapper extends BaseMapper<BlogComments> {
    /**
     * 将最新的数据展示在最上面
     * @param blogId
     * @return
     */
    @Select("select c.*,u.nick_name,u.icon from tb_blog_comments c left join tb_user u on c.user_id = u.id where c.blog_id = #{blogId}" )
//            "left join tb_blog_comments c1 on c1.pid = c.id where c.blog_id = #{blogId} order by c.blog_id desc")
    List<BlogComments> findCommentDetail(@Param("blogId") Long blogId);
}
