package com.example.zwj.mvpdemo.http;

import android.content.Context;
import android.util.Log;

import com.example.zwj.mvpdemo.common.Constant;
import com.example.zwj.mvpdemo.http.api.ApiResponse;
import com.example.zwj.mvpdemo.http.api.service.GankApiService;
import com.example.zwj.mvpdemo.http.cache.CacheProvider;
import com.example.zwj.mvpdemo.http.exception.ApiException;
import com.example.zwj.mvpdemo.http.parser.GsonTSpeaker;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.rx_cache2.internal.RxCache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 网络请求管理类
 * <b>创建时间</b> 2017/5/22 <br>
 *
 * @author zhouwenjun
 */
public class HttpManager {
    public static final String TAG = HttpManager.class.getSimpleName();
    private static final int DEFAULT_TIMEOUT = 5;
    private Retrofit mRetrofit;
    private GankApiService mGankApiService;
    private final CacheProvider cacheProvider;
    private volatile static HttpManager instance;
    private static Context mContext;

    private HttpManager() {
        HttpLoggingInterceptor.Level level = HttpLoggingInterceptor.Level.BODY;
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Log.i("HttpManager", message);
            }
        });
        loggingInterceptor.setLevel(level);
        //拦截请求和响应日志并输出，其实有很多封装好的日志拦截插件，大家也可以根据个人喜好选择。
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(loggingInterceptor);

        OkHttpClient okHttpClient = builder.build();

        mRetrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(Constant.GANK_HOME)
                .client(okHttpClient)
                .build();

        cacheProvider = new RxCache.Builder()
                .persistence(mContext.getFilesDir(), new GsonTSpeaker())
                .using(CacheProvider.class);
        mGankApiService = mRetrofit.create(GankApiService.class);
    }

    public static HttpManager getInstance() {
        if (instance == null) {
            synchronized (HttpManager.class) {
                if (instance == null) {
                    instance = new HttpManager();
                }
            }
        }
        return instance;
    }

    public static void init(Context context) {
        mContext = context;
    }

    public <T> void toSubscribe(Observable<ApiResponse<T>> o, Observer<T> s) {
        o.subscribeOn(Schedulers.io())
                .map(new Function<ApiResponse<T>, T>() {
                    @Override
                    public T apply(@NonNull ApiResponse<T> response) throws Exception {
//                        int code = Integer.parseInt(response.code);
//                        if (code != Constant.SUCCESS_CODE) {
//                            throw new ApiException(code, response.message);
//                        } else {
                        return response.results;
//                        }
                    }
                })
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s);
    }

    public GankApiService getGankApiService() {
        return mGankApiService;
    }

//    public void getDatasWithCache(Observer<TestBean> subscriber, int pno, int ps, String dtype, boolean update) {
//        toSubscribe(cacheProvider.getDatas(mApiService.getDatas(pno, ps, dtype), new EvictProvider(update)), subscriber);
//    }
//
//    public void getDatasNoCache(Observer<TestBean> subscriber, int pno, int ps, String dtype) {
//        toSubscribe(mApiService.getDatas(pno, ps, dtype), subscriber);
//    }

}
