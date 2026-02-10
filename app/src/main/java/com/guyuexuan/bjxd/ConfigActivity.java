package com.guyuexuan.bjxd;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.guyuexuan.bjxd.util.AppUtils;
import com.guyuexuan.bjxd.util.StorageUtil;

import java.util.Objects;

public class ConfigActivity extends AppCompatActivity {
    private StorageUtil storageUtil;
    private String aiApiKey;
    private String aiRequestUrl;
    private String aiModel;
    private String aiRequestParams;
    private String qinglongRequestUrl;
    private String qinglongClientId;
    private String qinglongClientSecret;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        // 设置标题
        setTitle(AppUtils.getAppNameWithVersion(this));

        storageUtil = StorageUtil.getInstance(this);

        SwitchMaterial manualAnswerSwitch = findViewById(R.id.manualAnswerSwitch);

        TextInputEditText aiApiKeyInput = findViewById(R.id.aiApiKeyInput);
        TextInputEditText aiRequestUrlInput = findViewById(R.id.aiRequestUrlInput);
        TextInputEditText aiModelInput = findViewById(R.id.aiModelInput);
        TextInputEditText aiRequestParamsInput = findViewById(R.id.aiRequestParamsInput);
        TextInputEditText qinglongRequestUrlInput = findViewById(R.id.qinglongRequestParamsInput);
        TextInputEditText qinglongClientIdInput = findViewById(R.id.qinglongClientIdInput);
        TextInputEditText qinglongClientSecretInput = findViewById(R.id.qinglongClientSecretInput);

        manualAnswerSwitch.setChecked(storageUtil.isManualAnswer());

        aiApiKey = storageUtil.getAiApiKey();
        aiRequestUrl = storageUtil.getAiRequestUrl();
        aiModel = storageUtil.getAiModel();
        aiRequestParams = storageUtil.getAiRequestParams();
        aiApiKeyInput.setText(aiApiKey);
        aiRequestUrlInput.setText(aiRequestUrl);
        aiModelInput.setText(aiModel);
        aiRequestParamsInput.setText(aiRequestParams);

        qinglongRequestUrl = storageUtil.getQinglongRequestUrl();
        qinglongClientId = storageUtil.getQinglongClientId();
        qinglongClientSecret = storageUtil.getQinglongClientSecret();
        qinglongRequestUrlInput.setText(qinglongRequestUrl);
        qinglongClientIdInput.setText(qinglongClientId);
        qinglongClientSecretInput.setText(qinglongClientSecret);

        findViewById(R.id.saveButton).setOnClickListener(v -> {
            // 答题：手动答题
            storageUtil.setManualAnswer(manualAnswerSwitch.isChecked());
            // 答题：AI 模型配置
            aiApiKey = Objects.requireNonNull(aiApiKeyInput.getText()).toString().trim();
            aiRequestUrl = Objects.requireNonNull(aiRequestUrlInput.getText()).toString().trim();
            aiModel = Objects.requireNonNull(aiModelInput.getText()).toString().trim();
            aiRequestParams = Objects.requireNonNull(aiRequestParamsInput.getText()).toString().trim();
            storageUtil.saveAiApiKey(aiApiKey);
            storageUtil.saveAiRequestUrl(aiRequestUrl);
            storageUtil.saveAiModel(aiModel);
            storageUtil.saveAiRequestParams(aiRequestParams);
            // 青龙面板配置
            qinglongRequestUrl = Objects.requireNonNull(qinglongRequestUrlInput.getText()).toString().trim();
            qinglongClientId = Objects.requireNonNull(qinglongClientIdInput.getText()).toString().trim();
            qinglongClientSecret = Objects.requireNonNull(qinglongClientSecretInput.getText()).toString().trim();
            storageUtil.saveQinglongRequestUrl(qinglongRequestUrl);
            storageUtil.saveQinglongClientId(qinglongClientId);
            storageUtil.saveQinglongClientSecret(qinglongClientSecret);
            // toast 提示保存成功
            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
            finish();
        });

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }
}
