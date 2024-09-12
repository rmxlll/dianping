package com.dianping.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dianping.blog.domain.po.Blog;
import com.dianping.common.UserDTO;
import com.dianping.common.utils.Result;

import java.util.List;
import java.util.Map;



public interface IBlogService extends IService<Blog> {

    List<Blog> queryHotBlog(Integer current);

    Blog queryBlogById(Long id);

    Boolean likeBlog(Long id);

    List<UserDTO> queryBlogLikes(Long id);

    Boolean saveBlog(Blog blog);

    List<UserDTO> queryBlogOfFollow(Long max, Integer offset);

    Map<String, Object> queryMyBlog(Integer current);
}
