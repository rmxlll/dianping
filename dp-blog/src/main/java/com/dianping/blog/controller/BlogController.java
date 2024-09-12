package com.dianping.blog.controller;


import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dianping.blog.domain.po.Blog;
import com.dianping.blog.service.IBlogService;
import com.dianping.common.UserDTO;
import com.dianping.common.utils.Result;
import com.dianping.common.utils.SystemConstants;
import com.dianping.common.utils.UserHolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/blog")
public class BlogController {

    @Resource
    private IBlogService blogService;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    /**
     * 上传博客
     * @param blog
     * @return
     */
    @PostMapping("/save")
    public Boolean saveBlog(@RequestBody Blog blog) {
        return blogService.saveBlog(blog);
    }

    @PutMapping("/like/{id}")
    public Boolean likeBlog(@PathVariable("id") Long id) {
        return blogService.likeBlog(id);
    }

    /**
     * 查看自己的博客
     * @param current 当前页数的变量
     * @return
     */
    @GetMapping("/of/me")
    public Map<String, Object> queryMyBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return blogService.queryMyBlog(current);
    }

    @GetMapping("/hot")
    public List<Blog> queryHotBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
      return blogService.queryHotBlog(current);
    }

    @GetMapping("/{id}")
    public Blog queryBlogById(@PathVariable("id")Long id){
        return blogService.queryBlogById(id);
    }

    @GetMapping("/likes/{id}")
    public List<UserDTO> queryBlogLikes(@PathVariable("id") Long id) {
        return blogService.queryBlogLikes(id);
    }

    /**
     * 点击他人主页时查询所有的博客
     * @param current
     * @param id
     * @return
     */
    // BlogController
    @GetMapping("/of/user")
    public Result queryBlogByUserId(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam("id") Long id) {
        // 根据用户查询
        Page<Blog> page = blogService.query()
                .eq("user_id", id).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        return Result.ok(records);
    }

    @PostMapping("/queryMe/read")
    public Result addToMessageList(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        UserDTO user = UserHolder.getUser();
        Long id = user.getId();
        Page<Blog> page = blogService.query().eq("user_id", id).
                eq("isRead", false).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("records", page.getRecords());
        resultData.put("totalPage", page.getPages());
        return Result.ok(resultData);
    }

    @GetMapping("/of/follow")
    public List<UserDTO> queryBlogOfFollow(@RequestParam("lastId") Long max, @RequestParam(value = "offset", defaultValue = "0") Integer offset){
        return blogService.queryBlogOfFollow(max, offset);
    }

    @PostMapping("/removeId")
    public Result queryBlogOfremoveId(
            @RequestParam("blogId") Long blogId){
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();
        Blog blog = blogService.query().eq("user_id", userId)
                .eq("id", blogId).one();
        blog.setIsRead(true);
        blogService.saveOrUpdate(blog);
        return Result.ok(blog);

    }

    @DeleteMapping("/del/{id}")
    public Result delete(@PathVariable Long id) {
        blogService.removeById(id);
        UserDTO user = UserHolder.getUser();
        Page<Blog> page = blogService.query().eq("user_id", user.getId())
                .page(new Page<>(1, SystemConstants.MAX_PAGE_SIZE));
        long total = page.getTotal();
        int pageSize = SystemConstants.MAX_PAGE_SIZE;
        int totalPages = (int) ((total + pageSize - 1) / pageSize);
        String key ="";
        for (int i = 1; i <= totalPages; i++) {

            key = "blog:of:"+user.getId()+"_"+i;
            stringRedisTemplate.delete(key);
        }
        return Result.ok();
    }
}
