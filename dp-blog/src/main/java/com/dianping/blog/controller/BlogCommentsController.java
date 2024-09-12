package com.dianping.blog.controller;



import com.dianping.blog.domain.po.BlogComments;
import com.dianping.blog.service.IBlogCommentsService;
import com.dianping.common.utils.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;



@RestController
@RequestMapping("/comment")
public class BlogCommentsController {
        @Resource
        private IBlogCommentsService commentService;



        // 新增或者更新评论
        @PostMapping("/save")
        public Boolean save(@RequestBody BlogComments comment) {
            return commentService.saveComments(comment);
        }

        /**
         * 删除评论
         * @param id
         * @return
         */
        @DeleteMapping("/{id}")
        public Boolean delete(@PathVariable Long id) {
            commentService.removeById(id);
            return commentService.removeById(id);
        }

        /**
         * 批量删除评论
         * @param ids
         * @return
         */

        @PostMapping("/del/batch")
        public Boolean deleteBatch(@RequestBody List<Long> ids) {
            return commentService.removeByIds(ids);
        }

        @GetMapping
        public List<BlogComments> findAll() {
            return commentService.list();
        }


        /**
         * 查看所有评论
         * @param blogId
         * @return
         */
        @GetMapping("/tree/{blogId}")
        public List<BlogComments> findTree(@PathVariable Long blogId) {  //查询所有的评论数据
            return commentService.findTree(blogId);
        }

        @GetMapping("/{id}")
        public BlogComments findOne(@PathVariable Long id) {
            return commentService.getById(id);
        }

    }
