package com.AppyKeepScreenOn;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.ComponentContainer;

@DesignerComponent(
    version = 1,
   // description = " ",
    category = ComponentCategory.EXTENSION,
    nonVisible = true
)

@SimpleObject(external = true)
public class AppyKeepScreenOn extends AndroidNonvisibleComponent implements Component
{
    public static final int VERSION = 1;
    private ComponentContainer container;
    private Context context;
    private static final String LOG_TAG = "AppyKeepScreenOn";
    private final Activity activity;
    private boolean keepScreenOn = false;
    private String result;
   
    public AppyKeepScreenOn(ComponentContainer container)
    {
        super(container.$form());
        this.container = container;
        this.context = container.$context();
        Log.d("AppyKeepScreenOn", "AppyKeepScreenOn Created");
        this.activity = container.$context();
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR)//返回屏幕是否设置为为常亮状态
    public String Result() { return this.result;}

    //@DesignerProperty(editorType = "boolean",defaultValue = "False")
	
    @SimpleProperty(description = "Check Enable to keep screen active and awake when App has focus. default is FALSE")
    public void KeepScreenOn(boolean enable)//设置屏幕状态为关或开
    {
        if(this.keepScreenOn != enable)
	{
            this.keepScreenOn = enable;
            if(enable) {this.container.$form().getWindow().addFlags(128);}
	    else {this.container.$form().getWindow().clearFlags(128);}
        }
   }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR)//返回屏幕是否正在为常亮状态
    public boolean KeepScreenOn() {return this.keepScreenOn;}
}
