
package com.guyuexuan.bjxd.util;

import com.google.gson.JsonObject;
import com.guyuexuan.bjxd.model.QinglongEnv;
import com.guyuexuan.bjxd.model.TaskStatus;
import com.guyuexuan.bjxd.model.User;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

public class ApiUtilTest {

    private final String token = "北京现代账号 token";

    @Test
    public void getUserInfo_success() throws IOException {
        User user = ApiUtil.getUserInfo(token);

        System.out.println(user);

        assertNotNull(user);
    }

    @Test
    public void getScore_success() throws IOException {
        JsonObject data = ApiUtil.getScore(token);

        System.out.println(data);

        assertNotNull(data);
    }

    @Test
    public void getTaskStatus_success() throws IOException {
        TaskStatus data = ApiUtil.getTaskStatus(token);

        System.out.println(data);

        assertNotNull(data);
    }

    @Test
    public void getQuestionInfo_success() throws IOException {
        JsonObject data = ApiUtil.getQuestionInfo(token);

        System.out.println(data);

        assertNotNull(data);
    }

    @Test
    public void askAI_success() throws IOException {
        String aiApiKey = "智谱 AI API Key";
        String aiRequestUrl = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
        String aiModel = "glm-4.5-flash";
        String aiRequestParams = "{\"do_sample\": false}";
        String question = "Q：冬季常有雨雪天气，沐飒的哪种驾驶模式可以通过控制变速箱，降低扭矩的输出，让车辆在冰雪路面上起步更平稳，减少轮胎打滑现象\nA. 经济模式\nB. 运动模式\nC. 雪地模式\nD. 舒适模式";

        String data = ApiUtil.askAI(aiApiKey, aiRequestUrl, aiModel, aiRequestParams, question);

        System.out.println(data);
    }

    @Test
    public void getQinglongToken_success() throws IOException {
        String base_url = "http://192.168.31.50:5700";
        String client_id = "re_Qx4A6-KjW";
        String client_secret = "-GxbEkQEQzX44x3jl5NGCoUv";

        String token = ApiUtil.getQinglongToken(base_url, client_id, client_secret);

        System.out.println(token);
    }

    @Test
    public void getQinglongEnvList_success() throws IOException {
        String base_url = "http://192.168.31.50:5700";
        String token = "33b48d38-14b1-429f-bac0-a50cbb1f5a40";
        String searchValue = "BJXD";

        QinglongEnv[] envList = ApiUtil.getQinglongEnvList(base_url, token, searchValue);
        int currentUserIndex = 0;
        for (QinglongEnv env : envList) {
            currentUserIndex++;
            System.out.println(currentUserIndex);
            System.out.println(env);
        }
        System.out.println("==========");
        System.out.println(currentUserIndex);
    }

    @Test
    public void createQinglongEnv_success() throws IOException {
        String base_url = "http://192.168.31.50:5700";
        String token = "33b48d38-14b1-429f-bac0-a50cbb1f5a40";
        String name = "TEST";
        String value = "Test Value";
        String remarks = "Test Remarks";

        QinglongEnv env = ApiUtil.createQinglongEnv(base_url, token, name, value, remarks);

        System.out.println(env);
    }

    @Test
    public void updateQinglongEnv_success() throws IOException {
        String base_url = "http://192.168.31.50:5700";
        String token = "33b48d38-14b1-429f-bac0-a50cbb1f5a40";
        long id = 21; // 示例环境变量 ID，需要根据实际情况调整
        String name = "TEST3";
        String value = "TEST3 value";
        String remarks = "TEST3 remarks";

        QinglongEnv env = ApiUtil.updateQinglongEnv(base_url, token, id, name, value, remarks);

        System.out.println(env);
        assertNotNull(env);
    }

    @Test
    public void deleteQinglongEnv_success() throws IOException {
        String base_url = "http://192.168.31.50:5700";
        String token = "33b48d38-14b1-429f-bac0-a50cbb1f5a40";
        long id = 23; // 示例环境变量 ID，需要根据实际情况调整

        boolean result = ApiUtil.deleteQinglongEnv(base_url, token, id);

        System.out.println("Delete result: " + result);
    }

}
