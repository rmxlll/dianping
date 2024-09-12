package com.dianping.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dianping.blog.domain.po.BlogComments;
import com.dianping.common.utils.Result;


import java.util.List;


public interface IBlogCommentsService extends IService<BlogComments> {


    List<BlogComments> findCommentDetail(Long blogId);

    Boolean saveComments(BlogComments comments);

    List<BlogComments> findTree(Long blogId);

}
