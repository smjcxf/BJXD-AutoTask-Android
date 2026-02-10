package com.guyuexuan.bjxd;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.guyuexuan.bjxd.model.QinglongEnv;
import com.guyuexuan.bjxd.model.User;
import com.guyuexuan.bjxd.util.ApiUtil;
import com.guyuexuan.bjxd.util.AppUtils;
import com.guyuexuan.bjxd.util.StorageUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyncActivity extends AppCompatActivity {
    private NestedScrollView nestedScrollView;
    private TextView logTextView;
    private Button actionButton;
    private TaskThread taskThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);

        // 设置标题
        setTitle(AppUtils.getAppNameWithVersion(this));

        // 保持屏幕常量
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        nestedScrollView = findViewById(R.id.nested_scroll_view);
        logTextView = findViewById(R.id.logTextView);
        actionButton = findViewById(R.id.actionButton);

        actionButton.setOnClickListener(v -> {
            // 关闭屏幕常量
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            if (taskThread != null && taskThread.isRunning()) {
                actionButton.setText("等待线程结束……");
                actionButton.setEnabled(false);
                taskThread.stopSync();
            } else {
                finish();
            }
        });

        taskThread = new TaskThread(StorageUtil.getInstance(this), this::appendLog, () -> runOnUiThread(() -> {
            // 关闭屏幕常量
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            actionButton.setText("返回");
            actionButton.setEnabled(true);
        }));
        taskThread.start();
    }

    private void appendLog(String log) {
        runOnUiThread(() -> {
            logTextView.append(log + "\n");
            // 滚动到底部 使用 NestedScrollView 提供的 fullScroll 方法
            nestedScrollView.post(() -> nestedScrollView.fullScroll(View.FOCUS_DOWN));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (taskThread != null) {
            taskThread.stopSync();
        }
    }

    private static class TaskThread extends Thread {
        private final List<User> userList;
        private final StorageUtil storageUtil;
        private final Consumer<String> logger;
        private final Runnable onComplete;
        private final List<String> wrongAnswers = new ArrayList<>(); // 错误答案列表
        private volatile boolean running = true;
        private int currentUserIndex = 0;

        public TaskThread(StorageUtil storageUtil, Consumer<String> logger, Runnable onComplete) {
            this.userList = storageUtil.getUserList();
            this.storageUtil = storageUtil;
            this.logger = logger;
            this.onComplete = onComplete;
        }

        @Override
        public void run() {
            try {
                if (userList.isEmpty()) {
                    logger.accept("🚨 用户列表为空，请先添加账号！\n");
                } else if (!this.storageUtil.checkQinglongSettings()) {
                    logger.accept("🚨 青龙面板参数缺失，请先配置！\n");
                } else {
                    // 显示总任务数
                    logger.accept(String.format(Locale.getDefault(), "共有 %d 个用户待同步", userList.size()));

                    String qinglong_base_url = storageUtil.getQinglongRequestUrl();
                    String qinglong_client_id = storageUtil.getQinglongClientId();
                    String qinglong_client_secret = storageUtil.getQinglongClientSecret();

                    Pattern pattern = Pattern.compile("^BJXD(\\d+)$");

                    logger.accept("\n============ 拉取云端账号 ============");
                    String qinglong_token = ApiUtil.getQinglongToken(qinglong_base_url, qinglong_client_id, qinglong_client_secret);
                    QinglongEnv[] qinglongEnvList = ApiUtil.getQinglongEnvList(qinglong_base_url, qinglong_token, "BJXD");
                    for (QinglongEnv qinglongEnv : qinglongEnvList) {
                        String envName = qinglongEnv.name();
                        if (envName != null) {
                            if (pattern.matcher(envName).matches()) {
                                logger.accept(String.format(Locale.getDefault(), "ID: %d Name: %s Remarks: %s", qinglongEnv.id(), qinglongEnv.name(), qinglongEnv.remarks()));
                            }
                        }
                    }

                    logger.accept("\n============ 推送本地账号 ============");
                    logger.accept(String.format(Locale.getDefault(), "RUN: 执行同步, 共 %d 个账号", userList.size()));
                    for (User user : userList) {
                        try {
                            checkShouldStop();
                            currentUserIndex++;
                            logger.accept(String.format(Locale.getDefault(), "\n======> 第 %d 个账号", currentUserIndex));
                            logger.accept(String.format("👻 用户名: %s  📱手机号: %s", user.getNickname(), user.getMaskedPhone()));
                            long envId = 0;
                            String envName = "BJXD" + currentUserIndex;
                            String envValue = user.getToken();
                            String envRemarks = "BJXD " + user.getMaskedPhone();

                            for (QinglongEnv qinglongEnv : qinglongEnvList) {
                                if (Objects.equals(qinglongEnv.name(), envName)) {
                                    envId = qinglongEnv.id();
                                    break;
                                }
                            }
                            if (envId > 0) { // 判断是否已存在
                                logger.accept(String.format(Locale.getDefault(), "云端更新 ID: %d Name: %s Remarks: %s", envId, envName, envRemarks));
                                QinglongEnv retQinglongEnv = ApiUtil.updateQinglongEnv(qinglong_base_url, qinglong_token, envId, envName, envValue, envRemarks);
                                logger.accept(String.format(Locale.getDefault(), "云端反馈 ID: %d Name: %s Remarks: %s", retQinglongEnv.id(), retQinglongEnv.name(), retQinglongEnv.remarks()));
                            } else {
                                logger.accept(String.format(Locale.getDefault(), "云端新建 Name: %s Remarks: %s", envName, envRemarks));
                                QinglongEnv retQinglongEnv = ApiUtil.createQinglongEnv(qinglong_base_url, qinglong_token, envName, envValue, envRemarks);
                                logger.accept(String.format(Locale.getDefault(), "云端反馈 ID: %d Name: %s Remarks: %s", retQinglongEnv.id(), retQinglongEnv.name(), retQinglongEnv.remarks()));
                            }
                        } catch (InterruptedException | IOException e) {
                            throw e;
                        } catch (Exception e) {
                            logger.accept("执行任务出错: " + e.getMessage());
                        }
                    }

                    logger.accept("\n============ 检查废弃账号 ============");
                    for (QinglongEnv qinglongEnv : qinglongEnvList) {
                        String envName = qinglongEnv.name();
                        if (envName != null) {
                            Matcher matcher = pattern.matcher(envName);
                            if (matcher.matches()) {
                                String numberStr = matcher.group(1);
                                if (numberStr != null) {
                                    if (Integer.parseInt(numberStr) > currentUserIndex) {
                                        logger.accept(String.format(Locale.getDefault(), "删除账号 ID: %d Name: %s Remarks: %s", qinglongEnv.id(), qinglongEnv.name(), qinglongEnv.remarks()));
                                        boolean ret = ApiUtil.deleteQinglongEnv(qinglong_base_url,qinglong_token, qinglongEnv.id());
                                        logger.accept(String.format(Locale.getDefault(), "删除结果 %s", ret ? "成功" : "失败"));
                                    }
                                }
                            }
                        }
                    }
                    logger.accept("\n============ 检查账号完毕 ============\n");

                }
            } catch (InterruptedException | IOException e) {
                logger.accept("🚨 同步被中断: " + e.getMessage());
            } finally {
                logger.accept("🚨 同步已停止，脚本结束。");
                stopSync();
                onComplete.run();
            }
        }

        /**
         * 检查任务是否需要停止
         *
         * @throws InterruptedException 如果任务需要停止则抛出此异常
         */
        private void checkShouldStop() throws InterruptedException {
            if (!running || isInterrupted()) {
                logger.accept("🚨 检测到停止命令，停止执行同步");
                throw new InterruptedException("Sync stopped");
            }
        }

        public void stopSync() {
            running = false;
        }

        public boolean isRunning() {
            return running;
        }

        /**
         * 执行用户同步
         */
        private void executeUserSync(int idx, User user, QinglongEnv qinglongEnv) throws InterruptedException {
            logger.accept(String.format("👻 用户名: %s  📱手机号: %s", user.getNickname(), user.getMaskedPhone()));
            logger.accept(String.format("🆔 用户hid: %s", user.getHid()));

            try {

            } catch (Exception e) {
                logger.accept("执行同步出错: " + e.getMessage());
            }
        }
    }
}
