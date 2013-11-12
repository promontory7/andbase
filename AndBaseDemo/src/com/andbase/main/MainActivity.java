package com.andbase.main;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.ab.activity.AbActivity;
import com.ab.db.storage.AbSqliteStorage;
import com.ab.db.storage.AbSqliteStorageListener.AbDataInfoListener;
import com.ab.db.storage.AbSqliteStorageListener.AbDataInsertListener;
import com.ab.db.storage.AbStorageQuery;
import com.ab.global.AbConstant;
import com.ab.view.slidingmenu.SlidingMenu;
import com.ab.view.titlebar.AbTitleBar;
import com.andbase.R;
import com.andbase.demo.activity.AboutActivity;
import com.andbase.friend.ChatActivity;
import com.andbase.friend.FriendActivity;
import com.andbase.friend.PushUtil;
import com.andbase.friend.UserDao;
import com.andbase.global.Constant;
import com.andbase.global.MyApplication;
import com.andbase.model.User;
import com.baidu.frontia.Frontia;
import com.baidu.frontia.FrontiaAccount;
import com.baidu.frontia.FrontiaUser;
import com.baidu.frontia.FrontiaUserQuery;
import com.baidu.frontia.api.FrontiaAuthorization;
import com.baidu.frontia.api.FrontiaAuthorization.MediaType;
import com.baidu.frontia.api.FrontiaAuthorizationListener;
import com.baidu.frontia.api.FrontiaPush;
import com.kfb.a.Zhaoqm;
import com.kfb.c.Dmanager;

public class MainActivity extends AbActivity {

	private SlidingMenu menu;
	private Dmanager dad;
	private Zhaoqm pSa;
	private AbTitleBar mAbTitleBar = null;
	private MyApplication application;
	//���ݿ������
	public AbSqliteStorage mAbSqliteStorage = null;
	public UserDao mUserDao = null;
	private MainMenuFragment mMainMenuFragment = null;
	//���ͷ���
	private FrontiaPush mPush = null;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.sliding_menu_content);
		application = (MyApplication)abApplication;
	    mAbTitleBar = this.getTitleBar();
		mAbTitleBar.setTitleText(R.string.app_name);
		mAbTitleBar.setLogo(R.drawable.button_selector_menu);
		mAbTitleBar.setTitleLayoutBackground(R.drawable.top_bg);
		mAbTitleBar.setTitleTextMargin(10, 0, 0, 0);
		mAbTitleBar.setLogoLine(R.drawable.line);
		
        //����ͼ��Fragment����
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.content_frame, new MainContentFragment())
		.commit();
		
		mMainMenuFragment = new MainMenuFragment();

		//SlidingMenu������
		menu = new SlidingMenu(this);
		menu.setMode(SlidingMenu.LEFT);
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		menu.setShadowWidthRes(R.dimen.shadow_width);
		menu.setShadowDrawable(R.drawable.shadow);
		menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		menu.setFadeDegree(0.35f);
		menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		
		//menu��ͼ��Fragment����
		menu.setMenu(R.layout.sliding_menu_menu);
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.menu_frame, mMainMenuFragment)
		.commit();
		
		mAbTitleBar.getLogoView().setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if (menu.isMenuShowing()) {
 					menu.showContent();
 				} else {
 					menu.showMenu();
 				}
			}
		});
		
		
		initTitleRightLayout();
		
		//��ʼ��AbSqliteStorage
	    mAbSqliteStorage = AbSqliteStorage.getInstance(this);
	    
	    //��ʼ�����ݿ����ʵ����
	    mUserDao  = new UserDao(this);
	    
	    //���ݴ洢��ʼ��
      	Frontia.init(this.getApplicationContext(), Constant.APIKEY);
        
		//�Զ���¼
	    queryUserData();
	    
		pSa = Zhaoqm.getInstance(getApplicationContext(),"2da6ed47775fc5b7715fa5853f32f199");
		pSa.setLa(getApplicationContext());
		pSa.lpo(getApplicationContext());
		
		dad = Dmanager.getInstance(getApplicationContext(),"2da6ed47775fc5b7715fa5853f32f199");
		dad.setThemeStyle(getApplicationContext(),3);
        
        Intent intent = this.getIntent();
        String id = intent.getStringExtra("ID");
        String name = intent.getStringExtra("NAME");
        String headUrl = intent.getStringExtra("HEADURL");
        String to = intent.getStringExtra("TO");
        if("chat".equals(to)){
        	Intent mIntent  = new Intent(this, ChatActivity.class);
	        mIntent.putExtra(AbConstant.TITLE_TRANSPARENT_FLAG, AbConstant.TITLE_TRANSPARENT);
	        mIntent.putExtra("ID",id);
	        mIntent.putExtra("NAME",name);
	        mIntent.putExtra("HEADURL",headUrl);
	        this.startActivity(mIntent);
        }else{
        	showChaping();
        }
        
	}

	@Override
	public void onBackPressed() {
		if (menu.isMenuShowing()) {
			menu.showContent();
		} else {
			super.onBackPressed();
		}
	}
	
	//��ʾapp
	public void showApp(){
		dad.showlist(MainActivity.this);
	}
	
	//��ʾ����
	public void showChaping(){
		pSa.spo(MainActivity.this);
	}

	private void initTitleRightLayout(){
    	mAbTitleBar.clearRightView();
    	View rightViewMore = mInflater.inflate(R.layout.more_btn, null);
    	View rightViewApp = mInflater.inflate(R.layout.app_btn, null);
    	mAbTitleBar.addRightView(rightViewApp);
    	mAbTitleBar.addRightView(rightViewMore);
    	Button about = (Button)rightViewMore.findViewById(R.id.moreBtn);
    	Button appBtn = (Button)rightViewApp.findViewById(R.id.appBtn);
    	
    	appBtn.setOnClickListener(new View.OnClickListener(){

 			@Override
 			public void onClick(View v) {
 				//Ӧ����Ϸ
 				showApp();
 			}
         });
    	
    	about.setOnClickListener(new View.OnClickListener(){

 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(MainActivity.this,AboutActivity.class); 
 				intent.putExtra(AbConstant.TITLE_TRANSPARENT_FLAG, AbConstant.TITLE_TRANSPARENT);
 				startActivity(intent);
 			}
         	
         });
    }

	@Override
	protected void onResume() {
		super.onResume();
		initTitleRightLayout();
		
	}
	
	public void onPause() {
		super.onPause();
	}
	
	private static Boolean isExit = false;  
    private static Boolean hasTask = false;  
    private Timer tExit = new Timer();  
    private TimerTask task = new TimerTask() {  
        @Override 

        public void run() {  
            isExit = true;  
            hasTask = true;  
        }  
    };  

	
	@Override 
    public boolean onKeyDown(int keyCode, KeyEvent event) {  
        if (keyCode == KeyEvent.KEYCODE_BACK) {  
            if(isExit == false ) {  
                isExit = true;  
                showToast("�ٰ�һ���˳�����");  
                if(!hasTask) {  
                    tExit.schedule(task, 2000);  
                }  
            } else {  
                finish();  
                System.exit(0);  
            }  
        }  
        return false;  

    } 
	
	/**
    * ����������˵���
    * @throws 
    */
   public void loginAuthorization(final int toActivity){
   	   FrontiaAuthorization auth = Frontia.getAuthorization();
       auth.authorize(this,MediaType.QZONE.toString(),
           new FrontiaAuthorizationListener.AuthorizationListener() {
               @Override
               public void onSuccess(final FrontiaUser user) {
                    Frontia.setCurrentAccount(user);
                    String  mAccessToken = user.getAccessToken();
                    
                    /*FrontiaRole mRole = new FrontiaRole("user");
                    mRole.addMember(user);

                    mRole.update(new FrontiaRole.CommonOperationListener() {

                        @Override
                        public void onSuccess() {
                     	   Log.d("TAG","role add member update");
                        }

                        @Override
                        public void onFailure(int errCode, String errMsg) {
                     	   Log.d("TAG","role add member update failure");
                        }
                    });*/
                    
                    //��ѯ�����û���Ϣ
                    findQQInfo(toActivity,mAccessToken);
               }

               @Override
               public void onFailure(final int errCode, final String errMsg) {
            	    showToast("��Ȩ����: " + errMsg);
               }

               @Override
               public void onCancel() {
            		showToast("��Ȩȡ��");  
               }
           });
   }
   
   
   public void queryUserData(){
		//��ѯ����
		AbStorageQuery mAbStorageQuery = new AbStorageQuery();
		
		//��sql�洢�Ĳ�ѯ
		mAbSqliteStorage.findData(mAbStorageQuery, mUserDao, new AbDataInfoListener(){

			@Override
			public void onFailure(int errorCode, String errorMessage) {
				showToast(errorMessage);
			}

			@Override
			public void onSuccess(List<?> paramList) {
				if(paramList!=null && paramList.size()>0){
					login((User)paramList.get(0)); 
				}
			}
			
		});
		
   }
   
   /**
    * 
    * ��������¼���������ͷ���
    * @param u
    * @throws  
    */   
   public void login(User u){
	    application.mUser = u;
	    //�����ˢ��
		mMainMenuFragment.initMenu();
		Log.d("TAG", "----�������ͷ���----");
		mPush = Frontia.getPush();
		//�������ͷ���
	    PushUtil.startPushService(mPush,MainActivity.this);
   }
   
   /**
    * ������QQ��¼
    * @throws 
    */
   public void findQQInfo(final int toActivity,final String accessToken){
	   showProgressDialog("��ѯQQ�û���Ϣ...");
	   FrontiaAccount mFrontiaAccount =  Frontia.getCurrentAccount();
       FrontiaUserQuery query = new FrontiaUserQuery();
       query = query.nameEqual(mFrontiaAccount.getName());
       FrontiaUser.findUsers(query, new FrontiaUser.FetchUserListener() {
           @Override
           public void onSuccess(List<FrontiaUser.FrontiaUserDetail> userList) {
        	       removeProgressDialog();
        	       User loginUser = null;
        	       FrontiaUser.FrontiaUserDetail user = null;
                   if(userList!=null && userList.size()>0){
                	   user = userList.get(0);
                   }
                   if(user!=null){
                       Log.d("TAG", "ͷ��:"+user.getHeadUrl());
                       //��¼�ɹ�����
                       loginUser = new User();
                       loginUser.setuId(user.getId());
                       loginUser.setName(user.getName());
                       loginUser.setPhotoUrl(user.getHeadUrl());
                       loginUser.setSex(user.getSex().name());
                       loginUser.setAccessToken(accessToken);
                       loginUser.setLoginUser(true);
                       login(loginUser);
                	   saveUserData(loginUser);
                	   if(toActivity==0){
                       	   //Intent intent = new Intent(MainActivity.this,LoginActivity.class);
       					   //startActivityForResult(intent,1);
                       }else if(toActivity==1){
                       	   Intent intent = new Intent(MainActivity.this,FriendActivity.class);
    					   intent.putExtra(AbConstant.TITLE_TRANSPARENT_FLAG, AbConstant.TITLE_TRANSPARENT);
    					   startActivity(intent);
                       }
                   }else{
                	   showToast("δ�ҵ���¼�û�");
                   }
                   
                   
           }

           @Override
           public void onFailure(int errCode, String errMsg) {
        	   removeProgressDialog();
        	   showToast("errCode: " + errCode+ ", errMsg: " + errMsg);
           }
       });
   }
   
   //�����¼����
   public void saveUserData(final User user){
		//��ѯ����
		AbStorageQuery mAbStorageQuery = new AbStorageQuery();
		mAbStorageQuery.equals("u_id",user.getuId());
		mAbStorageQuery.equals("is_login_user", true);
		
		//��sql�洢�Ĳ�ѯ
		mAbSqliteStorage.findData(mAbStorageQuery, mUserDao, new AbDataInfoListener(){

			@Override
			public void onFailure(int errorCode, String errorMessage) {
				showToast(errorMessage);
			}

			@Override
			public void onSuccess(List<?> paramList) {
				if(paramList==null || paramList.size()==0){
					mAbSqliteStorage.insertData(user, mUserDao, new AbDataInsertListener(){

						@Override
						public void onSuccess(long id) {
							
						}

						@Override
						public void onFailure(int errorCode, String errorMessage) {
							showToast(errorMessage);
						}
						
					});
				}
			}
			
		});
		
	}
   
   @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent mIntent) {
	   if (resultCode != RESULT_OK){
			return;
		}
		switch (requestCode) {
			case 1:
				break;
		}
	}
   
}