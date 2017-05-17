package fancy.hyypaysdk.pay.alipay;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;


import com.alipay.sdk.app.EnvUtils;
import com.alipay.sdk.app.PayTask;

import java.util.Map;

import fancy.hyypaysdk.pay.PayCallBack;

/**
 * Created by Hyy on 2016/10/9.
 */
public class AliPayUtils {


    private static final int SDK_PAY_FLAG = 1;

    private Activity activity;

    private PayCallBack callBack;

    private static AliPayUtils instance;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    @SuppressWarnings("unchecked")
                    PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                    /**
                     对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                     */
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                    String resultStatus = payResult.getResultStatus();

                    Log.i("instance", "========resultStatus=====" + resultStatus);
//                    LogUtil.MyLog("resultStatus", "=======resultStatus=====" + resultStatus);
                    // 判断resultStatus 为9000则代表支付成功
                    if (TextUtils.equals(resultStatus, "9000")) {
                        // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                        callBack.paySuccess();
                    } else if (TextUtils.equals(resultStatus, "6001")) {
                        callBack.payCancle();
                    } else {
                        // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                        callBack.payFaild(resultStatus);
                    }
                    break;
                }
                default:
                    break;
            }

            dismissValue();
        }
    };

    /**
     * 支付宝支付业务
     * <p>
     * orderInfo===app_id=2016073100133119&biz_content=%7B%22total_amount%22%3A%220.01%22%2C%22product_code%22%3A%22QUICK_MSECURITY_PAY%22%2C%22subject%22%3A%2210000010610%22%2C%22out_trade_no%22%3A%221487832564133734%22%7D&charset=UTF-8&format=json&method=alipay.trade.app.pay&notify_url=http%3A%2F%2Ftest.pay.fancyedu.net%2Fngkids%2Fnotify%2FaliAppPayNotify.json&sign_type=RSA&timestamp=2017-02-23+14%3A49%3A24&version=1.0&sign=PbjFtxcq65MSOtVe9t5HF48wJPSU9J1g2wXwK8su8ZxyZ3IA5vFooe8UF9ze5%2F%2BR6%2FTB71hqvqJLMN8AzMs7cmR2EERdtq1ebcb4el%2FZcNAzYfRX7TGJMM0xhIIets5753SanFyIOYXh%2BwUBz1pXz7ikude7APkF3P8zJLZaJ9E%3D
     */
    public void aliPay(Activity activity, final String orderInfo, PayCallBack callBack) {
        this.callBack = callBack;
        this.activity = activity;
        Runnable payRunnable = new Runnable() {
            @Override
            public void run() {
                PayTask alipay = new PayTask(AliPayUtils.this.activity);
                Map<String, String> result = alipay.payV2(orderInfo, true);
                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };

        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    private AliPayUtils() {

    }

    public static AliPayUtils getInstance() {
        if (instance == null) {
            instance = new AliPayUtils();
        }
        return instance;
    }

    private void dismissValue() {
        callBack = null;
        activity = null;
    }
}
