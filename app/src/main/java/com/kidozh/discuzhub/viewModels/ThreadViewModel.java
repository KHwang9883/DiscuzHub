package com.kidozh.discuzhub.viewModels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.daos.FavoriteThreadDao;
import com.kidozh.discuzhub.database.FavoriteThreadDatabase;
import com.kidozh.discuzhub.entities.ErrorMessage;
import com.kidozh.discuzhub.entities.FavoriteThread;
import com.kidozh.discuzhub.entities.PostInfo;
import com.kidozh.discuzhub.entities.ViewThreadQueryStatus;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.bbsPollInfo;
import com.kidozh.discuzhub.entities.ForumInfo;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.results.SecureInfoResult;
import com.kidozh.discuzhub.results.ThreadResult;
import com.kidozh.discuzhub.services.DiscuzApiService;
import com.kidozh.discuzhub.utilities.bbsParseUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.networkUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ThreadViewModel extends AndroidViewModel {
    private String TAG = ThreadViewModel.class.getSimpleName();



    private bbsInformation bbsInfo;
    private OkHttpClient client;
    private ForumInfo forum;
    private int tid;
    private forumUserBriefInfo userBriefInfo;

    public MutableLiveData<Boolean> isLoading= new MutableLiveData<>(false),
            hasLoadAll= new MutableLiveData<>(false);
    public MutableLiveData<Boolean> notifyLoadAll = new MutableLiveData<>(false);
    public MutableLiveData<String> formHash, errorText;
    public MutableLiveData<bbsPollInfo> pollInfoLiveData;
    public MutableLiveData<forumUserBriefInfo> bbsPersonInfoMutableLiveData;
    public MutableLiveData<List<PostInfo>> threadCommentInfoListLiveData;
    public MutableLiveData<ViewThreadQueryStatus> threadStatusMutableLiveData;
    public MutableLiveData<bbsParseUtils.DetailedThreadInfo> detailedThreadInfoMutableLiveData;
    public MutableLiveData<ThreadResult> threadPostResultMutableLiveData;
    private MutableLiveData<SecureInfoResult> secureInfoResultMutableLiveData;
    public LiveData<Boolean> isFavoriteThreadMutableLiveData;
    public LiveData<FavoriteThread> favoriteThreadLiveData;
    public MutableLiveData<ErrorMessage> errorMessageMutableLiveData = new MutableLiveData<>(null);
    FavoriteThreadDao dao;

    public ThreadViewModel(@NonNull Application application) {
        super(application);
        isLoading = new MutableLiveData<>(false);

        formHash = new MutableLiveData<>("");
        bbsPersonInfoMutableLiveData = new MutableLiveData<>();
        threadCommentInfoListLiveData = new MutableLiveData<>();
        hasLoadAll = new MutableLiveData<>(false);
        pollInfoLiveData = new MutableLiveData<>(null);
        threadStatusMutableLiveData = new MutableLiveData<>();
        errorText = new MutableLiveData<>("");
        detailedThreadInfoMutableLiveData = new MutableLiveData<>();
        threadPostResultMutableLiveData = new MutableLiveData<>();
        dao = FavoriteThreadDatabase.getInstance(application).getDao();

    }

    public void setBBSInfo(bbsInformation bbsInfo, forumUserBriefInfo userBriefInfo, ForumInfo forum, int tid){
        this.bbsInfo = bbsInfo;
        this.userBriefInfo = userBriefInfo;
        this.forum = forum;
        this.tid = tid;
        URLUtils.setBBS(bbsInfo);
        client = networkUtils.getPreferredClientWithCookieJarByUser(getApplication(),userBriefInfo);


        if(threadStatusMutableLiveData.getValue()==null){
            ViewThreadQueryStatus viewThreadQueryStatus = new ViewThreadQueryStatus(tid,1);
            threadStatusMutableLiveData.setValue(viewThreadQueryStatus);
        }
        isFavoriteThreadMutableLiveData = dao.isFavoriteItem(bbsInfo.getId(),userBriefInfo!=null?userBriefInfo.getUid():0,tid,"tid");
        if(userBriefInfo == null){
            favoriteThreadLiveData = dao.getFavoriteItemByTid(bbsInfo.getId(),0,tid,"tid");
        }
        else {
            favoriteThreadLiveData = dao.getFavoriteItemByTid(bbsInfo.getId(),userBriefInfo.getUid(),tid,"tid");
        }



        // bbsPersonInfoMutableLiveData.postValue(userBriefInfo);
    }

    public MutableLiveData<SecureInfoResult> getSecureInfoResultMutableLiveData(){
        if(secureInfoResultMutableLiveData == null){
            // load the secure info result
            secureInfoResultMutableLiveData = new MutableLiveData<>(null);
            // load the information
            getSecureInfo();
        }
        return secureInfoResultMutableLiveData;
    }

    public void getSecureInfo(){
        Retrofit retrofit = networkUtils.getRetrofitInstance(bbsInfo.base_url,client);
        DiscuzApiService service = retrofit.create(DiscuzApiService.class);
        Call<SecureInfoResult> secureInfoResultCall = service.secureResult("post");
        secureInfoResultCall.enqueue(new Callback<SecureInfoResult>() {
            @Override
            public void onResponse(Call<SecureInfoResult> call, Response<SecureInfoResult> response) {
                if(response.isSuccessful() && response.body()!=null){
                    secureInfoResultMutableLiveData.postValue(response.body());
                }
                else {
                    secureInfoResultMutableLiveData.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<SecureInfoResult> call, Throwable t) {
                secureInfoResultMutableLiveData.postValue(null);
            }
        });
    }

    public void getThreadDetail(ViewThreadQueryStatus viewThreadQueryStatus){
        isLoading.postValue(true);
        hasLoadAll.postValue(false);
        // bbsThreadStatus threadStatus = threadStatusMutableLiveData.getValue();

        threadStatusMutableLiveData.postValue(viewThreadQueryStatus);
        if(viewThreadQueryStatus.page == 1){
            // clear it first
            threadCommentInfoListLiveData.setValue(new ArrayList<>());
        }

        Retrofit retrofit = networkUtils.getRetrofitInstance(bbsInfo.base_url,client);
        DiscuzApiService service =  retrofit.create(DiscuzApiService.class);
        Call<ThreadResult> threadResultCall = service.viewThreadResult(viewThreadQueryStatus.generateQueryHashMap());

        threadResultCall.enqueue(new Callback<ThreadResult>() {
            @Override
            public void onResponse(Call<ThreadResult> call, Response<ThreadResult> response) {
                if(response.isSuccessful() && response.body()!=null){
                    int totalThreadSize = 0;
                    ThreadResult threadResult = response.body();
                    bbsParseUtils.DetailedThreadInfo detailedThreadInfo = null;
                    threadPostResultMutableLiveData.postValue(threadResult);
                    if(threadResult.message!=null){
                        errorMessageMutableLiveData.postValue(threadResult.message.toErrorMessage());
                    }
                    if(threadResult.threadPostVariables!=null){
                        // update formhash first
                        if(threadResult.threadPostVariables.formHash !=null){
                            formHash.postValue(threadResult.threadPostVariables.formHash);
                        }
                        // parse message
                        if(threadResult.message!=null){
                            errorText.postValue(threadResult.message.content);
                        }
                        // update user
                        if(threadResult.threadPostVariables!=null){
                            bbsPersonInfoMutableLiveData.postValue(threadResult.threadPostVariables.getUserBriefInfo());
                            // parse detailed info
                            detailedThreadInfo = threadResult.threadPostVariables.detailedThreadInfo;
                            detailedThreadInfoMutableLiveData.postValue(threadResult.threadPostVariables.detailedThreadInfo);

                            bbsPollInfo pollInfo = threadResult.threadPostVariables.pollInfo;
                            if(pollInfoLiveData.getValue() == null && pollInfo !=null){
                                pollInfoLiveData.postValue(pollInfo);

                            }
                            List<PostInfo> postInfoList = threadResult.threadPostVariables.postInfoList;
                            // remove null object
                            if(postInfoList.size()!=0){
                                if(viewThreadQueryStatus.page == 1){
                                    threadCommentInfoListLiveData.postValue(postInfoList);
                                    totalThreadSize = postInfoList.size();
                                }
                                else {
                                    List<PostInfo> currentThreadInfoList = threadCommentInfoListLiveData.getValue();
                                    if(currentThreadInfoList == null){
                                        currentThreadInfoList = new ArrayList<>();
                                    }
                                    currentThreadInfoList.addAll(postInfoList);
                                    threadCommentInfoListLiveData.postValue(currentThreadInfoList);
                                    totalThreadSize = currentThreadInfoList.size();

                                }
                            }
                            else {
                                if(viewThreadQueryStatus.page == 1 && threadResult.message !=null){
                                    errorText.postValue(getApplication().getString(R.string.parse_failed));
                                }
                                hasLoadAll.postValue(true);
                                // rollback
                                if(viewThreadQueryStatus.page != 1){
                                    viewThreadQueryStatus.page -=1;
                                    Log.d(TAG,"Roll back page when page to "+ viewThreadQueryStatus.page);
                                    threadStatusMutableLiveData.postValue(viewThreadQueryStatus);
                                }
                            }
                        }

                        // load all?
                        if(detailedThreadInfo !=null){
                            int maxThreadNumber = detailedThreadInfo.replies;
                            List<PostInfo> currentThreadInfoList = threadCommentInfoListLiveData.getValue();
                            int totalThreadCommentsNumber = 0;

                            if(currentThreadInfoList !=null){
                                totalThreadCommentsNumber = currentThreadInfoList.size();

                            }
                            else {

                            }

                            Log.d(TAG,"PAGE "+ viewThreadQueryStatus.page+" MAX POSITION "+maxThreadNumber +" CUR "+totalThreadCommentsNumber+ " "+totalThreadSize);
                            if(totalThreadSize >= maxThreadNumber +1){
                                hasLoadAll.postValue(true);
                            }
                            else {
                                hasLoadAll.postValue(false);
                            }
                        }
                    }
                    else {
                        errorMessageMutableLiveData.postValue(new ErrorMessage(
                                getApplication().getString(R.string.empty_result),
                                getApplication().getString(R.string.discuz_network_result_null)
                        ));
                    }
                }
                else {
                    errorMessageMutableLiveData.postValue(new ErrorMessage(String.valueOf(response.code()),
                            getApplication().getString(R.string.discuz_network_unsuccessful,response.message())));
                    if(viewThreadQueryStatus.page != 1){
                        viewThreadQueryStatus.page -=1;
                        Log.d(TAG,"Roll back page when page to "+ viewThreadQueryStatus.page);
                        threadStatusMutableLiveData.postValue(viewThreadQueryStatus);
                    }
                }
                isLoading.postValue(false);
            }

            @Override
            public void onFailure(Call<ThreadResult> call, Throwable t) {
                errorMessageMutableLiveData.postValue(new ErrorMessage(
                        getApplication().getString(R.string.discuz_network_failure_template),
                        t.getLocalizedMessage() == null?t.toString():t.getLocalizedMessage()
                ));
                isLoading.postValue(false);
            }
        });
        Log.d(TAG,"Send request to "+threadResultCall.request().url().toString());
    }
}
