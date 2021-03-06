package com.example.zwj.mvpdemo.mvp.live;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.example.zwj.mvpdemo.R;
import com.example.zwj.mvpdemo.adapter.HomeFollowAdapter;
import com.example.zwj.mvpdemo.base.BaseFragment;
import com.example.zwj.mvpdemo.bean.BannerBean;
import com.example.zwj.mvpdemo.bean.GankBean;
import com.example.zwj.mvpdemo.di.component.AppComponent;
import com.example.zwj.mvpdemo.di.component.DaggerHomeFollowComponent;
import com.example.zwj.mvpdemo.di.module.HomeFollowModule;
import com.example.zwj.mvpdemo.utils.FCLogger;
import com.example.zwj.mvpdemo.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import cn.bingoogolapple.bgabanner.BGABanner;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * 首页关注
 * <p>
 * <b>创建时间</b> 17/6/1 <br>
 *
 * @author zhouwenjun
 */
public class HomeFollowFragment extends BaseFragment<HomeFollowPresenter> implements HomeFollowContact.View, BaseQuickAdapter.RequestLoadMoreListener,
        SwipeRefreshLayout.OnRefreshListener, BGABanner.Adapter<ImageView, String>, BGABanner.Delegate<ImageView, String> {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    @BindView(R.id.swipeLayout_home_follow)
    SwipeRefreshLayout swipeLayoutFollow;
    @BindView(R.id.rv_home_follow)
    RecyclerView rvFollow;
    private HomeFollowAdapter homeFollowAdapter;
    private List<GankBean> datas = new ArrayList<>();
    private int pageCount = 2;
    private int pageIndex = 1;
    private BGABanner mBanner;
    private View notDataView;
    private View errorView;

    public HomeFollowFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static HomeFollowFragment newInstance(String param1, String param2) {
        HomeFollowFragment fragment = new HomeFollowFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void initView(View rootView) {
        swipeLayoutFollow.setOnRefreshListener(this);
        swipeLayoutFollow.setColorSchemeColors(Color.rgb(47, 223, 189));
        rvFollow.setLayoutManager(new LinearLayoutManager(mContext));
        homeFollowAdapter = new HomeFollowAdapter(datas, mContext);
        homeFollowAdapter.setOnLoadMoreListener(this, rvFollow);
        homeFollowAdapter.addHeaderView(getHeadView());
        homeFollowAdapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_RIGHT);
        rvFollow.setAdapter(homeFollowAdapter);
        rvFollow.setHasFixedSize(true);
        initErrorAndNoDataView();
    }

    /**
     * 初始化加载空布局和错误布局
     */
    private void initErrorAndNoDataView() {
        notDataView = mContext.getLayoutInflater().inflate(R.layout.empty_view, (ViewGroup) rvFollow.getParent(), false);
        notDataView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshData();
            }
        });
        errorView = mContext.getLayoutInflater().inflate(R.layout.error_view, (ViewGroup) rvFollow.getParent(), false);
        errorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshData();
            }
        });
    }

    /**
     * 获取rvHeadView
     *
     * @return
     */
    private View getHeadView() {
        View headerView = View.inflate(mContext, R.layout.layout_follow_head_view, null);
        mBanner = (BGABanner) headerView.findViewById(R.id.banner_home_follow);
        mBanner.setAdapter(this);
        mBanner.setDelegate(this);
        return headerView;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home_follow;
    }

    @Override
    protected void ComponentInject(AppComponent appComponent) {
        DaggerHomeFollowComponent
                .builder()
                .appComponent(appComponent)
                .homeFollowModule(new HomeFollowModule(this))
                .build()
                .inject(this);
    }

    @Override
    protected void lazyFetchData() {
        super.lazyFetchData();
        FCLogger.debug("lazyFetchData");
        swipeLayoutFollow.setRefreshing(true);
        refreshData();
        mPresenter.loadBannerData();
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void dismissLoading() {
        if (swipeLayoutFollow != null) {
            swipeLayoutFollow.setRefreshing(false);
        }
    }

    @Override
    public void showMsg(String msg) {

    }

    @Override
    public void showMsg(int msgId) {

    }

    /**
     * 刷新成功
     *
     * @param gankBeanList
     */
    @Override
    public void onRefreshSuccess(List<GankBean> gankBeanList) {
        if (gankBeanList == null || gankBeanList.size() == 0) {
            homeFollowAdapter.setEmptyView(notDataView);
            return;
        }
        FCLogger.debug("获取数据成功：" + gankBeanList.size());
        datas.clear();
        datas.addAll(gankBeanList);
        homeFollowAdapter.setNewData(datas);
        if (gankBeanList.size() == pageCount) {
            pageIndex += 1;
            homeFollowAdapter.setEnableLoadMore(true);
        }
    }

    @Override
    public void onLoadMoreSuccess(List<GankBean> gankBeanList) {
        swipeLayoutFollow.setEnabled(true);
        if (gankBeanList.size() == pageCount) {
            pageIndex += 1;
            homeFollowAdapter.loadMoreComplete();
        }
        homeFollowAdapter.addData(datas.size(), gankBeanList);
    }

    @Override
    public void onRefresh() {
        FCLogger.debug("正在刷新");
        //禁止上拉加载
        refreshData();
    }

    /**
     * 下拉刷新数据
     */
    private void refreshData() {
        homeFollowAdapter.setEmptyView(R.layout.loading_view, (ViewGroup) rvFollow.getParent());
        pageIndex = 1;
        homeFollowAdapter.setEnableLoadMore(false);
        mPresenter.getMeiZhiData(mContext, false, "福利", pageCount, pageIndex);
    }

    @Override
    public void onLoadMoreRequested() {
        mPresenter.getMeiZhiData(mContext, true, "福利", pageCount, pageIndex);
    }

    @Override
    public void onRefreshFailed() {
        if (swipeLayoutFollow != null) {
            swipeLayoutFollow.setRefreshing(false);
        }
        homeFollowAdapter.setEmptyView(errorView);
        ToastUtils.showShort("网络异常，请重试");
    }

    @Override
    public void onLoadMoreFailed() {
        if (homeFollowAdapter != null) {
            homeFollowAdapter.loadMoreFail();
        }
        ToastUtils.showShort("网络异常，请重试");
    }

    @Override
    public void onLoadBannerDataSuccess(BannerBean bannerBean) {
        mBanner.setData(bannerBean.imgs, bannerBean.tips);
    }

    @Override
    public void fillBannerItem(BGABanner banner, ImageView itemView, String model, int position) {
        Glide.with(this)
                .load(model)
                .bitmapTransform(new RoundedCornersTransformation(mContext, 3, 0))
                .placeholder(R.drawable.holder)
                .error(R.drawable.holder)
                .dontAnimate()
                .centerCrop()
                .into(itemView);
    }

    @Override
    public void onBannerItemClick(BGABanner banner, ImageView itemView, String model, int position) {
        Toast.makeText(mContext, "点击了第" + (position + 1) + "页", Toast.LENGTH_SHORT).show();
    }
}
