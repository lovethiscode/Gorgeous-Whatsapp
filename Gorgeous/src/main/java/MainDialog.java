import Env.DeviceEnv;
import ProtocolTree.*;
import Util.GorgeousLooper;
import Util.StringUtil;
import axolotl.AxolotlManager;
import com.google.protobuf.ByteString;
import jni.NoiseJni;
import jni.Register;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.json.JSONObject;
import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.logging.Log;
import org.whispersystems.libsignal.logging.SignalProtocolLogger;
import org.whispersystems.libsignal.logging.SignalProtocolLoggerProvider;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import org.whispersystems.libsignal.util.KeyHelper;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.*;
import java.io.File;
import java.util.Base64;
import java.util.Date;

public class MainDialog extends JDialog implements SignalProtocolLogger, GorgeousEngine.GorgeousEngineDelegate {
    private static final String TAG = MainDialog.class.getSimpleName();
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton login;
    private JButton logout;
    private JButton checkexist;
    private JButton requestcode;
    private JButton register;
    private JButton checkWhatsappVersion;
    private JTextArea history;
    private JTextField friend;
    private JTextArea sendcontent;
    private JButton selpicture;
    private JButton selvideo;
    private JButton selfile;
    private JButton 调试面试Button;
    private JTextArea debugpanel;
    GorgeousEngine engine_;


    public MainDialog() {
        GorgeousLooper.Instance().Init();
        String os = System.getProperty("os.name");
        if (os.startsWith("Linux")) {
            //sudo apt install libssl-dev
            //sudo apt-get install curl libcurl4-openssl-dev
            System.load(System.getProperty("user.dir") + "/jni/libNoiseJni.so");
        } else if (os.startsWith("Windows")) {
            System.load(System.getProperty("user.dir") + "\\jni\\libNoiseJni.dll");
        } else if (os.startsWith("Mac")) {
            System.load(System.getProperty("user.dir") + "/jni/libNoiseJni.dylib");
        }



        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
        }

        SignalProtocolLoggerProvider.setProvider(this);

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (null == engine_) {
                    debugpanel.append("需要先登录");
                    debugpanel.append("\r\n");
                    return;
                }
                engine_.SendText(friend.getText(), sendcontent.getText());
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        login.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                // 创建一个默认的文件选取器
                JFileChooser fileChooser = new JFileChooser();
                // 设置默认显示的文件夹为当前文件夹
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir") + "/out/"));
                // 设置文件选择的模式（只选文件、只选文件夹、文件和文件均可选）
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                // 设置是否允许多选
                fileChooser.setMultiSelectionEnabled(false);
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    // 如果点击了"确定", 则获取选择的文件路径
                    File file = fileChooser.getSelectedFile();
                    engine_ = new GorgeousEngine( file.getAbsolutePath(), MainDialog.this, null, System.getProperty("user.dir") + "/out");
                    boolean start = engine_.StartEngine();
                    if (!start) {
                        Log.i(TAG, "start engine error");
                    }
                }
            }
        });
        logout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (null == engine_) {
                    debugpanel.append("还没有登陆");
                    debugpanel.append("\r\n");
                    return;
                }
                engine_.StopEngine();
                engine_ = null;
            }
        });
        checkexist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    DeviceEnv.AndroidEnv.Builder envBuilder = DeviceEnv.AndroidEnv.newBuilder();
                    //固定不变
                    envBuilder.setEdgeRoutingInfo(ByteString.copyFrom(Base64.getDecoder().decode("CAUIDA==")));
                    envBuilder.setChatDnsDomain("fb");

                    Env.DeviceEnv.UserAgent.Builder userAgentBuilder = envBuilder.getUserAgentBuilder();
                    userAgentBuilder.setPlatform(DeviceEnv.Platform.ANDROID);
                    userAgentBuilder.setReleaseChannel(DeviceEnv.ReleaseChannel.RELEASE);
                    //随机生成
                    userAgentBuilder.setMcc("452");
                    userAgentBuilder.setMnc("001");
                    userAgentBuilder.setOsVersion("10");
                    userAgentBuilder.setManufacturer("motorola");
                    userAgentBuilder.setDevice("potter");
                    userAgentBuilder.setOsBuildNumber("QQ1B.200205.002");
                    userAgentBuilder.setPhoneId("pokl1mju-2d13-4761-933f-f197bdedfcae");
                    userAgentBuilder.setLocaleLanguageIso6391("vi");
                    userAgentBuilder.setLocaleCountryIso31661Alpha2("VN");

                    //支持的版本号，不要修改
                    Env.DeviceEnv.AppVersion.Builder appVersionBuilder = userAgentBuilder.getAppVersionBuilder();
                    appVersionBuilder.setPrimary(2);
                    appVersionBuilder.setSecondary(21);
                    appVersionBuilder.setTertiary(5);
                    appVersionBuilder.setQuaternary(14);

                    //保留下面代码
                    IdentityKeyPair clientKeyPair = KeyHelper.generateIdentityKeyPair();
                    DeviceEnv.KeyPair.Builder keyPairBuilder = DeviceEnv.KeyPair.newBuilder();
                    keyPairBuilder.setStrPubKey(ByteString.copyFrom(clientKeyPair.getPublicKey().serialize(), 1, 32));
                    keyPairBuilder.setStrPrivateKey(ByteString.copyFrom(clientKeyPair.getPrivateKey().serialize()));
                    envBuilder.setClientStaticKeyPair(keyPairBuilder);

                    //电话号码加区号
                    envBuilder.setFullphone("84705506058");
                    //随机生成
                    envBuilder.setFdid("a9cfd076-b4ee-4f48-bdcc-c4890f4df279");
                    //20 个字节， 随机生成
                    envBuilder.setExpid(ByteString.copyFrom("11111111111111111111".getBytes()));

                    AxolotlManager axolotlManager = new AxolotlManager(System.getProperty("user.dir") + "/out/new.db", envBuilder.build().toByteArray());
                    JSONObject existParams = new JSONObject();

                    existParams.put("cc", "84");
                    existParams.put("in", "705506058");
                    existParams.put("id", "12345678912345678912");
                    existParams.put("client_static_pubkey", StringUtil.Base64UrlEncode(envBuilder.getClientStaticKeyPair().getStrPubKey().toByteArray()));
                    existParams.put("regid", StringUtil.Base64UrlEncode(GorgeousEngine.AdjustId(axolotlManager.getLocalRegistrationId())));
                    existParams.put("identity", StringUtil.Base64UrlEncode(axolotlManager.GetIdentityKeyPair().getPublicKey().serialize(),1, 32));

                    SignedPreKeyRecord signedPreKeyRecord = axolotlManager.LoadLatestSignedPreKey(false);
                    existParams.put("skey_id", StringUtil.Base64UrlEncode(GorgeousEngine.AdjustId(signedPreKeyRecord.getId())));
                    existParams.put("skey_val",StringUtil.Base64UrlEncode(signedPreKeyRecord.getKeyPair().getPublicKey().serialize(),1 ,32));
                    existParams.put("skey_sig",StringUtil.Base64UrlEncode(signedPreKeyRecord.getSignature()));
                    String encodeParams = Register.EncodeExistRequest(existParams.toString());
                    JSONObject encodeJosn = new JSONObject(encodeParams);
                    if (encodeJosn.getInt("code") != 0) {
                        Log.e(TAG, encodeJosn.getString("desc"));
                        return;
                    }
                    okhttp3.Request request = new okhttp3.Request.Builder()
                            .url(encodeJosn.getString("request"))
                            .addHeader("User-Agent", GetUserAgent(envBuilder))
                            .addHeader("Accept-Charset", "UTF-8")
                            .addHeader("Content-Type", "application/x-www-form-urlencoded")
                            .get().build();
                    OkHttpClient okHttpClient = new OkHttpClient().newBuilder().hostnameVerifier((s, sslSession) -> true).build();;
                    Response response = okHttpClient.newCall(request).execute();
                    if (response.isSuccessful()) {
                        JSONObject responseJson = new JSONObject(response.body().string());
                        String status = responseJson.getString("status");
                        if (status.equals("ok")) {
                            envBuilder.setEdgeRoutingInfo(ByteString.copyFrom(Base64.getDecoder().decode(responseJson.getString("edge_routing_info"))));
                            String chatDnsDomain = responseJson.getString("chat_dns_domain");
                            if (StringUtil.isEmpty(chatDnsDomain)) {
                                envBuilder.setChatDnsDomain("fb");
                            } else {
                                envBuilder.setChatDnsDomain(chatDnsDomain);
                            }
                            axolotlManager.SetBytesSetting("env", envBuilder.build().toByteArray());
                        }
                    } else {

                    }
                }
                catch (Exception e) {
                    Log.e(TAG, e.getLocalizedMessage());
                }
            }
        });

        requestcode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
            }
        });
        register.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                engine_.Test();
            }
        });
        checkWhatsappVersion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                //proxyType :  "socks5" or "http"
                String status = NoiseJni.CheckWhatsappVersion("", "",0,"","");
                if (!status.equals("success")) {
                    Log.e(TAG, status);
                }
                history.append(status);
                history.append("\r\n");
            }
        });
        requestcode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    AxolotlManager axolotlManager = new AxolotlManager(System.getProperty("user.dir") + "/out/new.db",null);
                    byte[] envBuffer =  axolotlManager.GetBytesSetting("env");
                    DeviceEnv.AndroidEnv.Builder envBuilder = DeviceEnv.AndroidEnv.parseFrom(envBuffer).toBuilder();

                    JSONObject codeRequestParams = new JSONObject();

                    codeRequestParams.put("cc", "84");
                    codeRequestParams.put("in", "705506058");
                    codeRequestParams.put("id", "12345678912345678912");
                    codeRequestParams.put("client_static_pubkey", StringUtil.Base64UrlEncode(envBuilder.getClientStaticKeyPair().getStrPubKey().toByteArray()));
                    codeRequestParams.put("regid", StringUtil.Base64UrlEncode(GorgeousEngine.AdjustId(axolotlManager.getLocalRegistrationId())));
                    codeRequestParams.put("identity", StringUtil.Base64UrlEncode(axolotlManager.GetIdentityKeyPair().getPublicKey().serialize(),1, 32));

                    SignedPreKeyRecord signedPreKeyRecord = axolotlManager.LoadLatestSignedPreKey(false);
                    codeRequestParams.put("skey_id", StringUtil.Base64UrlEncode(GorgeousEngine.AdjustId(signedPreKeyRecord.getId())));
                    codeRequestParams.put("skey_val",StringUtil.Base64UrlEncode(signedPreKeyRecord.getKeyPair().getPublicKey().serialize(),1 ,32));
                    codeRequestParams.put("skey_sig",StringUtil.Base64UrlEncode(signedPreKeyRecord.getSignature()));
                    String encodeParams = Register.CodeRequest(codeRequestParams.toString());
                    JSONObject encodeJosn = new JSONObject(encodeParams);
                    if (encodeJosn.getInt("code") != 0) {
                        Log.e(TAG, encodeJosn.getString("desc"));
                        return;
                    }
                    okhttp3.Request request = new okhttp3.Request.Builder()
                            .url(encodeJosn.getString("request"))
                            .addHeader("User-Agent", GetUserAgent(envBuilder))
                            .addHeader("Accept-Charset", "UTF-8")
                            .addHeader("Content-Type", "application/x-www-form-urlencoded")
                            .get().build();
                    OkHttpClient okHttpClient = new OkHttpClient().newBuilder().hostnameVerifier((s, sslSession) -> true).build();;
                    Response response = okHttpClient.newCall(request).execute();
                    if (response.isSuccessful()) {
                        JSONObject responseJson = new JSONObject(response.body().string());
                        String status = responseJson.getString("status");
                        if (status.equals("ok")) {
                            envBuilder.setEdgeRoutingInfo(ByteString.copyFrom(Base64.getDecoder().decode(responseJson.getString("edge_routing_info"))));
                            String chatDnsDomain = responseJson.getString("chat_dns_domain");
                            if (StringUtil.isEmpty(chatDnsDomain)) {
                                envBuilder.setChatDnsDomain("fb");
                            } else {
                                envBuilder.setChatDnsDomain(chatDnsDomain);
                            }
                            axolotlManager.SetBytesSetting("env", envBuilder.build().toByteArray());
                        }
                    } else {

                    }
                }
                catch (Exception e) {

                }
            }
        });
        register.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    AxolotlManager axolotlManager = new AxolotlManager(System.getProperty("user.dir") + "/out/new.db",null);
                    byte[] envBuffer =  axolotlManager.GetBytesSetting("env");
                    DeviceEnv.AndroidEnv.Builder envBuilder = DeviceEnv.AndroidEnv.parseFrom(envBuffer).toBuilder();

                    JSONObject registerParams = new JSONObject();

                    registerParams.put("cc", "84");
                    registerParams.put("in", "705506058");
                    registerParams.put("id", "12345678912345678912");
                    registerParams.put("client_static_pubkey", StringUtil.Base64UrlEncode(envBuilder.getClientStaticKeyPair().getStrPubKey().toByteArray()));
                    registerParams.put("regid", StringUtil.Base64UrlEncode(GorgeousEngine.AdjustId(axolotlManager.getLocalRegistrationId())));
                    registerParams.put("identity", StringUtil.Base64UrlEncode(axolotlManager.GetIdentityKeyPair().getPublicKey().serialize(),1, 32));

                    SignedPreKeyRecord signedPreKeyRecord = axolotlManager.LoadLatestSignedPreKey(false);
                    registerParams.put("skey_id", StringUtil.Base64UrlEncode(GorgeousEngine.AdjustId(signedPreKeyRecord.getId())));
                    registerParams.put("skey_val",StringUtil.Base64UrlEncode(signedPreKeyRecord.getKeyPair().getPublicKey().serialize(),1 ,32));
                    registerParams.put("skey_sig",StringUtil.Base64UrlEncode(signedPreKeyRecord.getSignature()));

                    //sms code
                    registerParams.put("code", "123456");
                    String encodeParams = Register.Register(registerParams.toString());
                    JSONObject encodeJosn = new JSONObject(encodeParams);
                    if (encodeJosn.getInt("code") != 0) {
                        Log.e(TAG, encodeJosn.getString("desc"));
                        return;
                    }
                    okhttp3.Request request = new okhttp3.Request.Builder()
                            .url(encodeJosn.getString("request"))
                            .addHeader("User-Agent", GetUserAgent(envBuilder))
                            .addHeader("Accept-Charset", "UTF-8")
                            .addHeader("Content-Type", "application/x-www-form-urlencoded")
                            .get().build();
                    OkHttpClient okHttpClient = new OkHttpClient().newBuilder().hostnameVerifier((s, sslSession) -> true).build();;
                    Response response = okHttpClient.newCall(request).execute();
                    if (response.isSuccessful()) {
                        JSONObject responseJson = new JSONObject(response.body().string());
                        String status = responseJson.getString("status");
                        if (status.equals("ok")) {
                            envBuilder.setEdgeRoutingInfo(ByteString.copyFrom(Base64.getDecoder().decode(responseJson.getString("edge_routing_info"))));
                            String chatDnsDomain = responseJson.getString("chat_dns_domain");
                            if (StringUtil.isEmpty(chatDnsDomain)) {
                                envBuilder.setChatDnsDomain("fb");
                            } else {
                                envBuilder.setChatDnsDomain(chatDnsDomain);
                            }
                            axolotlManager.SetBytesSetting("env", envBuilder.build().toByteArray());
                        }
                    } else {

                    }
                }
                catch (Exception e) {

                }
            }
        });
        selpicture.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (null == engine_) {
                    debugpanel.append("需要先登录");
                    debugpanel.append("\r\n");
                    return;
                }
                // 创建一个默认的文件选取器
                JFileChooser fileChooser = new JFileChooser();
                // 设置默认显示的文件夹为当前文件夹
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir") + "/out/"));
                // 设置文件选择的模式（只选文件、只选文件夹、文件和文件均可选）
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setFileFilter(new FileNameExtensionFilter("图片文件(*.png, *.jpeg)", "png", "jpeg"));
                // 设置是否允许多选
                fileChooser.setMultiSelectionEnabled(false);
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    // 如果点击了"确定", 则获取选择的文件路径
                    File file = fileChooser.getSelectedFile();
                    engine_.SendMedia(friend.getText(), file.getAbsolutePath(), "image");
                }
            }
        });
        selvideo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (null == engine_) {
                    debugpanel.append("需要先登录");
                    debugpanel.append("\r\n");
                    return;
                }
                // 创建一个默认的文件选取器
                JFileChooser fileChooser = new JFileChooser();
                // 设置默认显示的文件夹为当前文件夹
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir") + "/out/"));
                // 设置文件选择的模式（只选文件、只选文件夹、文件和文件均可选）
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setFileFilter(new FileNameExtensionFilter("视频文件(*.mp4)", "mp4"));
                // 设置是否允许多选
                fileChooser.setMultiSelectionEnabled(false);
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    // 如果点击了"确定", 则获取选择的文件路径
                    File file = fileChooser.getSelectedFile();
                    engine_.SendMedia(friend.getText(), file.getAbsolutePath(), "video");
                }
            }
        });
        selfile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (null == engine_) {
                    debugpanel.append("需要先登录");
                    debugpanel.append("\r\n");
                    return;
                }
                // 创建一个默认的文件选取器
                JFileChooser fileChooser = new JFileChooser();
                // 设置默认显示的文件夹为当前文件夹
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir") + "/out/"));
                // 设置文件选择的模式（只选文件、只选文件夹、文件和文件均可选）
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                // 设置是否允许多选
                fileChooser.setMultiSelectionEnabled(false);
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    // 如果点击了"确定", 则获取选择的文件路径
                    File file = fileChooser.getSelectedFile();
                    engine_.SendMedia(friend.getText(), file.getAbsolutePath(), "document");
                }
            }
        });
        调试面试Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                debugpanel.setVisible(!debugpanel.isVisible());
            }
        });
    }

    String GetUserAgent(DeviceEnv.AndroidEnv.Builder envBuilder) {
        return String.format("WhatsApp/%d.%d.%d.%d Android/%s Device/%s-%s",
                envBuilder.getUserAgent().getAppVersion().getPrimary(),
                envBuilder.getUserAgent().getAppVersion().getSecondary(),
                envBuilder.getUserAgent().getAppVersion().getTertiary(),
                envBuilder.getUserAgent().getAppVersion().getQuaternary(),
                envBuilder.getUserAgent().getOsVersion(),
                envBuilder.getUserAgent().getManufacturer().replace("-", ""),
                envBuilder.getUserAgent().getDevice().replace("-", ""));
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        MainDialog dialog = new MainDialog();
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

        System.exit(0);
    }

    @Override
    public void log(int priority, String tag, String message) {
        debugpanel.append(message);
        debugpanel.append("\r\n");
    }

    @Override
    public void OnLogin(int code, ProtocolTreeNode desc) {
        history.append(desc.toString());
        history.append("\r\n");
    }

    @Override
    public void OnDisconnect(String desc) {
        history.append(desc);
        history.append("\r\n");
    }

    @Override
    public void OnSync(ProtocolTreeNode content) {
        history.append(content.toString());
        history.append("\r\n");
    }

    @Override
    public void OnPacketResponse(String type, ProtocolTreeNode content) {
        history.append(content.toString());
        history.append("\r\n");
    }
}
