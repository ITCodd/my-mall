package com.github.excel;

import org.springframework.stereotype.Component;

/**
 * @author: hjp
 * Date: 2020/5/28
 * Description:
 */
@Component
public class RedisEasyExcelFeedback extends AbstractEasyExcelFeedback {

//    @Autowired
//    private StringRedisTemplate redisTemplate;

    @Override
    public ExcelResult getExcelResult(String feedbackId) {
//        String data = redisTemplate.opsForValue().get(Constant.REDIS_Feedback_PREFIX + feedbackId);
//        if(StringUtils.isBlank(data)){
//            return null;
//        }
//        ExcelResult result = JSON.parseObject(data, ExcelResult.class);
//        return result;
        return null;
    }

    @Override
    public void saveFeedbackmsg(ExcelResult result) {
//        if(StringUtils.isNotBlank(result.getFeedbackId())){
//            redisTemplate.delete(Constant.REDIS_Feedback_PREFIX+result.getFeedbackId());
//        }
//        result.setFeedbackId(RandomUtil.getUUID());
//        redisTemplate.opsForValue().set(Constant.REDIS_Feedback_PREFIX+result.getFeedbackId(), JSON.toJSONString(result),1, TimeUnit.HOURS);
    }


}
