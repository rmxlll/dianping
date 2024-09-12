package com.dianping.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dianping.common.UserDTO;
import com.dianping.common.utils.Result;
import com.dianping.common.utils.UserHolder;
import com.dianping.user.domain.dto.LoginFormDTO;
import com.dianping.user.domain.po.User;
import com.dianping.user.domain.vo.UserVO;
import com.dianping.user.exception.CodeException;
import com.dianping.user.exception.PhoneValidException;
import com.dianping.user.mapper.UserMapper;
import com.dianping.user.service.IUserService;

import com.dianping.user.utils.RegexUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.dianping.common.utils.RedisConstants.*;
import static com.dianping.common.utils.SystemConstants.USER_NICK_NAME_PREFIX;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Boolean sendCode(String phone) {
        // 1.验证手机号
        if(RegexUtils.isPhoneInvalid(phone)){
            return false;
        }
        // 2.符合生成验证码
        String code = RandomUtil.randomNumbers(6);
        // 3. 保存验证码到redis，设置过期时长
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY+ phone,code,LOGIN_CODE_TTL, TimeUnit.MINUTES);
        // 5.发送验证码
        log.debug("发送短信验证码成功，验证码：{}", code);
        return true;
    }

    @Override
    public String login(LoginFormDTO loginForm, HttpSession session) {
        // 1.验证手机号
        if(RegexUtils.isPhoneInvalid(loginForm.getPhone())){
            throw new PhoneValidException("手机号无效");
        }
        // 2.从redis中获取验证码并校验
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + loginForm.getPhone());
        String code = loginForm.getCode();
        if(cacheCode==null){
            throw new CodeException("验证码已过期");
        }
        if (code==null||!cacheCode.equals(code)){
            throw new CodeException("验证码错误");
        }
        // 3.一致的话，根据手机号查询用户
        User user = this.query().eq("phone",loginForm.getPhone()).one();
        // 4.如果用户不存在，则创建新用户并保存
        if(user==null){
            user = this.createUserWithPhone(loginForm.getPhone());
        }
        // 5.保存用户信息到redis中
        // 随机生成token
        String token = UUID.randomUUID().toString(true);
        // 存储到redis
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String,Object> userMap = BeanUtil.beanToMap(userDTO,new HashMap<>(), CopyOptions.create().setIgnoreNullValue(true).setFieldValueEditor((fieldName,fieldValue) ->fieldValue.toString()));
        String tokenKey = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        // Map<Object,Object> userMapp = stringRedisTemplate.opsForHash().entries(tokenKey);
        // System.out.println("Storing to Redis: " + userMapp);
        // 6.设置token有效期
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);
        return token;
    }

    /**
     * 签到功能
     * @return
     */
    @Override
    public Boolean sign() {
        // 获取当前用户
        Long userId = UserHolder.getUser().getId();
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        // 拼接key
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = USER_SIGN_KEY+userId+keySuffix;
        // 获取今天是本月的第几天
        int dayOfMonth = now.getDayOfMonth();
        stringRedisTemplate.opsForValue().setBit(key,dayOfMonth-1,true);
        return true;
    }

    /**
     * 统计连续签到
     * @return
     */
    @Override
    public Boolean signCount() {
        // 获取当前用户
        Long userId = UserHolder.getUser().getId();
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        // 拼接key
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = USER_SIGN_KEY+userId+keySuffix;
        // 获取今天是本月的第几天
        int dayOfMonth = now.getDayOfMonth();
        List<Long> result = stringRedisTemplate.opsForValue().bitField(key,BitFieldSubCommands.create()
                .get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth)).valueAt(0));
        // 2、判断签到记录是否存在
        if (result == null || result.isEmpty()) {
            // 没有任何签到结果
            return true;
        }

        // 3、获取本月的签到数（List<Long>是因为BitFieldSubCommands是一个子命令，可能存在多个返回结果，这里我们知识使用了Get，
        // 可以明确只有一个返回结果，即为本月的签到数，所以这里就可以直接通过get(0)来获取）
        Long num = result.get(0);
        if (num == null || num == 0) {
            // 二次判断签到结果是否存在，让代码更加健壮
            return true;
        }
        // 4、循环遍历，获取连续签到的天数（从当前天起始）
        int count = 0;
        while (true) {
            // 让这个数字与1做与运算，得到数字的最后一个bit位，并且判断这个bit位是否为0
            if ((num & 1) == 0) {
                // 如果为0，说明未签到，结束
                break;
            } else {
                // 如果不为0，说明已签到，计数器+1
                count++;
            }
            // 把数字右移一位，抛弃最后一个bit位，继续下一个bit位
            num >>>= 1;
        }
        return true;
    }

    @Override
    public Boolean logout() {
        if (UserHolder.getUser()!=null){
            UserHolder.removeUser();
        }
        return true;
    }

    @Override
    public Boolean changeName(String nickName) {
        UserDTO user = UserHolder.getUser();
        Long id = user.getId();
        User user1 = this.query().eq("id",id).one();
        user1.setNickName(nickName);
        this.updateById(user1);
        UserHolder.updateNickname(nickName);
        String key = "user:"+id;
        stringRedisTemplate.delete(key);
        return true;
    }

    @Override
    public UserDTO getMe() {
        UserDTO userdto = UserHolder.getUser();
        Long userId = userdto.getId();
        String key = "user:"+userId;
        String cache = stringRedisTemplate.opsForValue().get(key);
        User user = null;
        if(cache!=null){
            user = JSONUtil.toBean(cache,User.class);
        } else{
            user = this.getById(userId);
            if(user==null){
                // 没有详情应该是第一次查看详情
                return null;
            }
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(user));
            UserHolder.removeUser();
        }
        // 返回
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        UserHolder.saveUser(userdto);
        return userDTO;
    }

    @Override
    public List<User> list(QueryWrapper<UserVO> queryWrapper) {
        // Convert QueryWrapper<UserVO> to QueryWrapper<User>
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        BeanUtil.copyProperties(queryWrapper, userQueryWrapper);

        // Use MyBatis Plus list method to query the database
        return this.list(userQueryWrapper);
    }

    private User createUserWithPhone(String phone) {
        // 1.创建用户
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        // 2.保存用户
        this.save(user);
        return user;
    }

}
