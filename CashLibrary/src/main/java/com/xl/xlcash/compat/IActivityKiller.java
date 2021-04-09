/*
 * *   
 *   Created by wyl on 3/13/21 1:09 PM  
 *  project_name:CashDemo     
 *  创建时间：3/13/21 1:09 PM   
 *  修改人：wyl   
 *  修改时间：3/13/21 1:09 PM    
 *  修改备注：
 *  版权所有违法必究   
 *  @version    
 * /
 */
package com.xl.xlcash.compat;

import android.os.Message;


public interface IActivityKiller {

    void finishLaunchActivity(Message message);

    void finishResumeActivity(Message message);

    void finishPauseActivity(Message message);

    void finishStopActivity(Message message);


}
