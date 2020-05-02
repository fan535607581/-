package com.puravidaapps;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.RouteInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.OnDestroyListener;
import com.google.appinventor.components.runtime.ReplForm;
import com.google.appinventor.components.runtime.util.AsynchUtil;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@DesignerComponent(version=14, description="WiFi Manager Extension. Version 14 as of 2019-10-21.", category=ComponentCategory.EXTENSION, nonVisible=true, iconName="https://puravidaapps.com/images/taifun16.png", helpUrl="https://puravidaapps.com/wifi.php")
@SimpleObject(external=true)
@UsesPermissions(permissionNames="android.permission.ACCESS_WIFI_STATE, android.permission.CHANGE_WIFI_STATE")
public class TaifunWiFi extends AndroidNonvisibleComponent
  implements Component, OnDestroyListener
{
  public static final int VERSION = 14;
  private ComponentContainer container;
  private Context context;
  private final Activity activity;
  private static final String LOG_TAG = "TaifunWiFi";
  private boolean suppressSuccessMessage;
  private boolean suppressWarnings;
  private static WifiManager wm;
  private boolean isRepl = false;
  private WiFiReceiverScan wiFiReceiverScan;

  public TaifunWiFi(ComponentContainer container)
  {
    super(container.$form());
    if ((this.form instanceof ReplForm)) {
      this.isRepl = true;
    }
    this.container = container;
    this.context = container.$context();
    this.activity = container.$context();
    wm = (WifiManager)this.context.getSystemService("wifi");
    Log.d("TaifunWiFi", "TaifunWiFi Created");
  }

  @SimpleProperty(category=PropertyCategory.BEHAVIOR, description="whether Success Message should be suppressed. Used in Enable and Disable method.")
  public boolean SuppressSuccessMessage()
  {
    return this.suppressSuccessMessage;
  }

  @DesignerProperty(editorType="boolean", defaultValue="false")
  @SimpleProperty
  public void SuppressSuccessMessage(boolean suppressSuccessMessage)
  {
    this.suppressSuccessMessage = suppressSuccessMessage;
  }

  @SimpleProperty(category=PropertyCategory.BEHAVIOR, description="whether Warnings should be suppressed")
  public boolean SuppressWarnings()
  {
    return this.suppressWarnings;
  }

  @DesignerProperty(editorType="boolean", defaultValue="false")
  @SimpleProperty
  public void SuppressWarnings(boolean suppressWarnings)
  {
    this.suppressWarnings = suppressWarnings;
  }

  @SimpleFunction(description="Return the local IP Address. Returns wifi ip if its enabled else the cellular one.")
  public String LocalIP()
  {
    if (wm.isWifiEnabled())
    {
      WifiInfo wi = wm.getConnectionInfo();
      int ipAddress = wi.getIpAddress();
      String ip = String.format("%d.%d.%d.%d", new Object[] { Integer.valueOf(ipAddress & 0xFF), Integer.valueOf(ipAddress >> 8 & 0xFF), Integer.valueOf(ipAddress >> 16 & 0xFF), Integer.valueOf(ipAddress >> 24 & 0xFF) });
      return ip;
    }

    try
    {
      for (en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
        NetworkInterface intf = (NetworkInterface)en.nextElement();
        for (enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
          InetAddress inetAddress = (InetAddress)enumIpAddr.nextElement();
          Log.i("TaifunWiFi", new StringBuilder().append("inetAddress.getHostAddress(): ").append(inetAddress.getHostAddress()).toString());

          if ((!inetAddress.isLoopbackAddress()) && ((inetAddress instanceof Inet4Address))) {
            Log.i("TaifunWiFi", new StringBuilder().append("return inetAddress.getHostAddress(): ").append(inetAddress.getHostAddress()).toString());
            return inetAddress.getHostAddress();
          }
        }
      }
    }
    catch (Exception e)
    {
      Enumeration en;
      Enumeration enumIpAddr;
      return "";
    }
    return "";
  }

  @SimpleFunction(description="Get current WiFi state: true or false.")
  public boolean IsEnabled()
  {
    WifiInfo wi = wm.getConnectionInfo();
    try {
      if ((!wm.isWifiEnabled()) || (wi.getSSID() == null)) {
        return false;
      }
      return true;
    } catch (Exception e) {
    }
    return false;
  }

  @SimpleFunction(description="Enable WiFi. You can hide the success message after setting the suppressSuccessMessage property to false.")
  public void Enable()
  {
    if (this.isRepl) {
      Log.w("TaifunWiFi", "You have to build the app to be able to use this method!");
      if (!this.suppressWarnings)
        Toast.makeText(this.context, "You have to build the app to be able to use this method!", 0).show();
    }
    else {
      wm.setWifiEnabled(true);
      if (!this.suppressSuccessMessage)
        Toast.makeText(this.context, "WiFi enabled.", 0).show();
    }
  }

  @SimpleFunction(description="Disable WiFi. You can hide the success message after setting the suppressSuccessMessage property to false.")
  public void Disable()
  {
    if (this.isRepl) {
      Log.w("TaifunWiFi", "You have to build the app to be able to use this method!");
      if (!this.suppressWarnings)
        Toast.makeText(this.context, "You have to build the app to be able to use this method!", 0).show();
    }
    else {
      wm.setWifiEnabled(false);
      if (!this.suppressSuccessMessage)
        Toast.makeText(this.context, "WiFi disabled.", 1).show();
    }
  }

  @SimpleFunction(description="Get current WiFi SSID (Service Set Identifier).")
  public String SSID()
  {
    String ssid = "";
    ConnectivityManager connManager = (ConnectivityManager)this.context.getSystemService("connectivity");

    NetworkInfo networkInfo = connManager.getNetworkInfo(1);
    if (networkInfo.isConnected()) {
      WifiInfo wi = wm.getConnectionInfo();
      if ((wi != null) && (!TextUtils.isEmpty(wi.getSSID()))) {
        ssid = wi.getSSID().replace("\"", "");
      }
    }

    if ((!wm.isWifiEnabled()) && 
      (!this.suppressWarnings)) {
      Toast.makeText(this.context, "WiFi is disabled, can't get current SSID.", 0).show();
    }

    Log.d("TaifunWiFi", new StringBuilder().append("SSID: ").append(ssid).toString());
    return ssid;
  }

  @SimpleFunction(description="Connect to a SSID (Service Set Identifier).")
  public void ConnectSSID(final String ssid, final String password)
  {
    Log.d("TaifunWiFi", "ConnectSSID");
    if (TextUtils.isEmpty(ssid)) {
      Log.d("TaifunWiFi", "ssid is empty");
      if (!this.suppressWarnings)
        Toast.makeText(this.context, "Can't connect to an empty SSID", 0).show();
    }
    else if (!wm.isWifiEnabled()) {
      if (!this.suppressWarnings)
        Toast.makeText(this.context, new StringBuilder().append("WiFi is disabled, can't connect to ").append(ssid).toString(), 0).show();
    }
    else if (this.isRepl) {
      if (!this.suppressWarnings)
        Toast.makeText(this.context, "You have to build the app to be able to use this method!", 0).show();
    }
    else {
      AsynchUtil.runAsynchronously(new Runnable()
      {
        public void run() {
          TaifunWiFi.this.AsyncConnectSSID(ssid, password);
        }
      });
    }
  }

  private void AsyncConnectSSID(String ssid, String password)
  {
    Log.d("TaifunWiFi", "AsyncConnectSSID");
    WifiConfiguration wc = new WifiConfiguration();
    wc.SSID = String.format("\"%s\"", new Object[] { ssid });

    if (password.isEmpty())
    {
      Log.v("TaifunWiFi", "open network");
      wc.allowedKeyManagement.set(0);
      wc.allowedProtocols.set(1);
      wc.allowedProtocols.set(0);
      wc.allowedAuthAlgorithms.clear();
      wc.allowedPairwiseCiphers.set(2);
      wc.allowedPairwiseCiphers.set(1);
      wc.allowedGroupCiphers.set(0);
      wc.allowedGroupCiphers.set(1);
      wc.allowedGroupCiphers.set(3);
      wc.allowedGroupCiphers.set(2);
    } else {
      Log.v("TaifunWiFi", "WPA");

      wc.allowedProtocols.set(1);
      wc.allowedProtocols.set(0);
      wc.allowedKeyManagement.set(1);
      wc.allowedPairwiseCiphers.set(2);
      wc.allowedPairwiseCiphers.set(1);
      wc.allowedGroupCiphers.set(0);
      wc.allowedGroupCiphers.set(1);
      wc.allowedGroupCiphers.set(3);
      wc.allowedGroupCiphers.set(2);
      wc.preSharedKey = String.format("\"%s\"", new Object[] { password });
    }

    List list = wm.getConfiguredNetworks();
    boolean found = false;
    int netId = 0;
    for (WifiConfiguration i : list) {
      if ((i.SSID != null) && (i.SSID.equals(new StringBuilder().append("\"").append(ssid).append("\"").toString()))) {
        found = true;
        wm.disconnect();
        netId = i.networkId;
        Log.d("TaifunWiFi", new StringBuilder().append("ssid: ").append(ssid).append(" found in configured SSIDs, netId: ").append(netId).toString());
        wm.enableNetwork(netId, true);
        wm.reconnect();
        break;
      }
    }
    if (!found)
    {
      netId = wm.addNetwork(wc);
      Log.d("TaifunWiFi", new StringBuilder().append("remember ssid: ").append(ssid).append(" in configured SSIDs, netId: ").append(netId).toString());

      wm.disconnect();
      wm.enableNetwork(netId, true);
      wm.reconnect();
    }

    if (netId == -1) {
      this.activity.runOnUiThread(new Runnable()
      {
        public void run() {
          TaifunWiFi.this.AfterWifiNegotiation(false);
        } } );
    }
    else {
      final boolean successful = checkWifiNegotiation(netId);
      this.activity.runOnUiThread(new Runnable()
      {
        public void run() {
          TaifunWiFi.this.AfterWifiNegotiation(successful);
        }
      });
    }
  }

  @SimpleFunction(description="Get a list of configured SSIDs (Service Set Identifiers). WiFi must be enabled for this.")
  public Object ConfiguredSSIDs()
  {
    Log.d("TaifunWiFi", "ConfiguredSSIDs");
    try {
      List networks = wm.getConfiguredNetworks();
      List ssids = new ArrayList();

      if (networks != null) {
        for (WifiConfiguration result : networks) {
          ssids.add(result.SSID.replace("\"", ""));
        }
      }
      return ssids;
    } catch (Exception e) {
      if (!this.suppressWarnings) {
        Toast.makeText(this.context, e.getMessage(), 0).show();
      }
      e.printStackTrace();
      Log.e("TaifunWiFi", e.getMessage(), e);
    }return null;
  }

  @SimpleFunction(description="Get MAC address")
  public String MacAddress()
  {
    Log.d("TaifunWiFi", "MacAddress");
    String macAddress = "02:00:00:00:00:00";
    WifiInfo wi = wm.getConnectionInfo();
    macAddress = wi.getMacAddress();

    if (macAddress.equals("02:00:00:00:00:00")) {
      Log.d("TaifunWiFi", "MacAddress workaround");
      String interfaceName = "wlan0";
      try {
        List interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
        for (NetworkInterface intf : interfaces)
          if ((interfaceName == null) || 
            (intf.getName().equalsIgnoreCase(interfaceName)))
          {
            byte[] mac = intf.getHardwareAddress();
            if (mac == null) return "02:00:00:00:00:00";
            StringBuilder buf = new StringBuilder();
            for (int idx = 0; idx < mac.length; idx++)
              buf.append(String.format("%02X:", new Object[] { Byte.valueOf(mac[idx]) }));
            if (buf.length() > 0) buf.deleteCharAt(buf.length() - 1);
            return buf.toString();
          }
      } catch (Exception e) {
        Log.e("TaifunWiFi", e.getMessage(), e);
        if (!this.suppressWarnings) {
          Toast.makeText(this.context, e.getMessage(), 0).show();
        }
      }
      return "02:00:00:00:00:00";
    }
    return macAddress;
  }

  @SimpleFunction(description="Get current WiFi BSSID (the MAC address of the access point).")
  public String BSSID()
  {
    String bssid = "";
    ConnectivityManager connManager = (ConnectivityManager)this.context.getSystemService("connectivity");

    NetworkInfo networkInfo = connManager.getNetworkInfo(1);
    if (networkInfo.isConnected()) {
      WifiInfo wi = wm.getConnectionInfo();
      if ((wi != null) && (!TextUtils.isEmpty(wi.getBSSID()))) {
        bssid = wi.getBSSID();
      }
    }

    if ((!wm.isWifiEnabled()) && 
      (!this.suppressWarnings)) {
      Toast.makeText(this.context, "WiFi is disabled, can't get current BSSID.", 0).show();
    }

    Log.d("TaifunWiFi", new StringBuilder().append("SSID: ").append(bssid).toString());
    return bssid;
  }

  @SimpleFunction(description="Get signal strength (RSSI) in a range between 0 and 100.")
  public int SignalStrength()
  {
    Log.d("TaifunWiFi", "SignalStrength");
    int MIN_RSSI = -100;
    int MAX_RSSI = -55;
    int levels = 101;
    WifiManager wifi = (WifiManager)this.context.getSystemService("wifi");
    WifiInfo info = wifi.getConnectionInfo();
    int rssi = info.getRssi();

    if (Build.VERSION.SDK_INT >= 14) {
      return WifiManager.calculateSignalLevel(info.getRssi(), levels);
    }

    if (rssi <= MIN_RSSI)
      return 0;
    if (rssi >= MAX_RSSI) {
      return levels - 1;
    }
    float inputRange = MAX_RSSI - MIN_RSSI;
    float outputRange = levels - 1;
    return (int)((rssi - MIN_RSSI) * outputRange / inputRange);
  }

  @SimpleFunction(description="Get current connection info.")
  public String ConnectionInfo()
  {
    Log.d("TaifunWiFi", "ConnectionInfo");
    ConnectivityManager connManager = (ConnectivityManager)this.context.getSystemService("connectivity");

    NetworkInfo networkInfo = connManager.getNetworkInfo(1);

    if ((!wm.isWifiEnabled()) && 
      (!this.suppressWarnings)) {
      Toast.makeText(this.context, "WiFi is disabled, can't get current connection info.", 0).show();
    }

    String connectionInfo = "";
    if (networkInfo.isConnected()) {
      WifiInfo wi = wm.getConnectionInfo();
      if (wi != null) {
        connectionInfo = wi.toString();
      }
    }
    return connectionInfo;
  }

  @SimpleFunction(description="Check, if 5 GHz Band is supported.")
  public boolean Is5GHzBandSupported()
  {
    Log.d("TaifunWiFi", "Is5GHzBandSupported");

    if (!wm.isWifiEnabled()) {
      if (!this.suppressWarnings) {
        Toast.makeText(this.context, "WiFi is disabled, can't get check if 5 Ghz band is supported.", 0).show();
      }
      return false;
    }
    return wm.is5GHzBandSupported();
  }

  @SimpleFunction(description="Return the IP Address of access point.")
  public String AccessPointIP()
  {
    DhcpInfo dhcpInfo = wm.getDhcpInfo();
    byte[] ipAddress = convert2Bytes(dhcpInfo.serverAddress);
    try {
      return InetAddress.getByAddress(ipAddress).getHostAddress();
    }
    catch (UnknownHostException e) {
      if (!this.suppressWarnings) {
        Toast.makeText(this.context, e.getMessage(), 0).show();
      }
    }
    return "";
  }

  private static byte[] convert2Bytes(int hostAddress)
  {
    byte[] addressBytes = { (byte)(0xFF & hostAddress), (byte)(0xFF & hostAddress >> 8), (byte)(0xFF & hostAddress >> 16), (byte)(0xFF & hostAddress >> 24) };

    return addressBytes;
  }

  @SimpleFunction(description="Disconnect.")
  public void Disconnect()
  {
    wm.disconnect();
  }

  @SimpleFunction(description="Return a list of DNS servers (primary and secondary) of the current network.")
  public Object DnsServers()
  {
    Log.d("TaifunWiFi", "DnsServers");

    DnsServersDetector d = new DnsServersDetector();
    String[] dnsArray = d.getServers();
    List dnsList = new ArrayList();
    for (String dns : dnsArray) {
      dnsList.add(dns.toString());
    }
    return dnsList;
  }

  @SimpleFunction(description="Get a list of available SSIDs (Service Set Identifiers). WiFi must be enabled for this.")
  public void AvailableSSIDs()
  {
    Log.d("TaifunWiFi", "AvailableSSIDs called");
    if (wm.isWifiEnabled()) {
      if (this.isRepl) {
        Log.w("TaifunWiFi", "You have to build the app to be able to use this method!");
        if (!this.suppressWarnings)
          Toast.makeText(this.context, "You have to build the app to be able to use this method!", 0).show();
      }
      else {
        this.wiFiReceiverScan = new WiFiReceiverScan(null);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.SCAN_RESULTS");
        this.context.registerReceiver(this.wiFiReceiverScan, intentFilter);
        wm.startScan();
      }
    } else {
      if (!this.suppressWarnings) {
        Toast.makeText(this.context, "WiFi is disabled, can't get list of available SSIDs.", 0).show();
      }
      Log.d("TaifunWiFi", "WiFi is disabled, exit.");
    }
  }

  @SimpleEvent(description="indicating that Available SSIDs (Service Set Identifiers) have been scanned.")
  public void GotAvailableSSIDs(Object availableSSIDs, String bestSSID, Object correspondingRSSIs, Object correspondingBSSIs)
  {
    Log.d("TaifunWiFi", "GotAvailableSSIDs");

    EventDispatcher.dispatchEvent(this, "GotAvailableSSIDs", new Object[] { availableSSIDs, bestSSID, correspondingRSSIs, correspondingBSSIs });
    unregisterReceiverScan();
  }

  private void unregisterReceiverScan()
  {
    if (this.wiFiReceiverScan != null) {
      Log.d("TaifunWiFi", "unregisterReceiver");
      this.context.unregisterReceiver(this.wiFiReceiverScan);
      this.wiFiReceiverScan = null;
    }
  }

  public void onDestroy()
  {
    Log.d("TaifunWiFi", "onDestroy");
    unregisterReceiverScan();
  }

  private static boolean checkWifiNegotiation(int netId)
  {
    Log.d("TaifunWiFi", new StringBuilder().append("checkWifiNegotiation, netId: ").append(netId).toString());
    boolean startedHandshake = false;
    boolean successful = false;

    for (int i = 0; i < 30; i++) {
      Log.d("TaifunWiFi", new StringBuilder().append("checkWifiNegotiation, i: ").append(i).toString());
      try {
        Thread.sleep(300L);
      } catch (InterruptedException e) {
        e.printStackTrace();
        Log.e("TaifunWiFi", e.getMessage());
      }

      SupplicantState currentState = wm.getConnectionInfo().getSupplicantState();
      if ((!startedHandshake) && (currentState.equals(SupplicantState.FOUR_WAY_HANDSHAKE))) {
        startedHandshake = true;
      } else if (startedHandshake) {
        if (currentState.equals(SupplicantState.DISCONNECTED))
          break;
        if (currentState.equals(SupplicantState.COMPLETED)) {
          successful = true;
          break;
        }
      }
      wm.enableNetwork(netId, true);
    }

    if ((!successful) && (wm.getConnectionInfo().getSupplicantState().equals(SupplicantState.COMPLETED))) {
      successful = true;
    }
    return successful;
  }

  @SimpleEvent(description="Check if the negotiation with the WifiConfiguration was successful, Returns true or false.")
  public void AfterWifiNegotiation(boolean successful)
  {
    Log.d("TaifunWiFi", "AfterWifiNegotiation");
    EventDispatcher.dispatchEvent(this, "AfterWifiNegotiation", new Object[] { Boolean.valueOf(successful) });
  }

  @SimpleFunction(description="Remove a SSID from the network list. Note: starting from Android M, apps are not allowed to remove networks that they did not create.")
  public boolean RemoveSSID(String ssid)
  {
    Log.d("TaifunWiFi", "RemoveSSID");
    boolean found = false;
    int netId;
    if (this.isRepl) {
      if (!this.suppressWarnings)
        Toast.makeText(this.context, "You have to build the app to be able to use this method!", 0).show();
    }
    else {
      List list = wm.getConfiguredNetworks();
      netId = 0;
      for (WifiConfiguration i : list) {
        if ((i.SSID != null) && (i.SSID.equals(new StringBuilder().append("\"").append(ssid).append("\"").toString()))) {
          found = true;
          wm.disconnect();
          netId = i.networkId;
          Log.d("TaifunWiFi", new StringBuilder().append("ssid: ").append(ssid).append(" found in configured SSIDs, netId: ").append(netId).toString());
          wm.removeNetwork(netId);
          wm.saveConfiguration();
          break;
        }
      }
    }
    return found;
  }

  private class WiFiReceiverScan extends BroadcastReceiver
  {
    private WiFiReceiverScan()
    {
    }

    public void onReceive(Context context, Intent intent)
    {
      Log.d("TaifunWiFi", "onReceive, API version: " + Build.VERSION.SDK_INT);

      List scanresult = TaifunWiFi.wm.getScanResults();
      List ssids = new ArrayList();
      List rssis = new ArrayList();
      List bssis = new ArrayList();

      ScanResult bestResult = null;
      String bestResultSSID = "";
      Log.d("TaifunWiFi", "intent: " + intent + ", size: " + scanresult.size() + ", enabled: " + TaifunWiFi.wm.isWifiEnabled());

      for (ScanResult result : scanresult) {
        String ssid = result.SSID.replace("\"", "");
        Log.d("TaifunWiFi", ssid);
        ssids.add(ssid);

        rssis.add(String.valueOf(result.level));
        bssis.add(String.valueOf(result.BSSID));

        if ((bestResult == null) || (WifiManager.compareSignalLevel(bestResult.level, result.level) < 0)) {
          bestResult = result;
          bestResultSSID = bestResult.SSID.replace("\"", "");
        }
      }
      TaifunWiFi.this.GotAvailableSSIDs(ssids, bestResultSSID, rssis, bssis);
    }
  }

  public class DnsServersDetector
  {
    private static final String TAG = "DnsServersDetector";
    private final String[] FACTORY_DNS_SERVERS = { "0.0.0.0", "0.0.0.0" };
    private static final String METHOD_EXEC_PROP_DELIM = "]: [";

    public DnsServersDetector()
    {
    }

    public String[] getServers()
    {
      String[] result = getServersMethodSystemProperties();
      Log.d("TaifunWiFi", "DNS, method1");
      if ((result != null) && (result.length > 0)) {
        Log.d("TaifunWiFi", "result: " + result.toString());
        return result;
      }

      result = getServersMethodConnectivityManager();
      Log.d("TaifunWiFi", "DNS, method2");
      if ((result != null) && (result.length > 0)) {
        Log.d("TaifunWiFi", "result: " + result.toString());
        return result;
      }

      result = getServersMethodExec();
      Log.d("TaifunWiFi", "DNS, method3");
      if ((result != null) && (result.length > 0)) {
        Log.d("TaifunWiFi", "result: " + result.toString());
        return result;
      }

      Log.d("TaifunWiFi", "DNS, return default, " + this.FACTORY_DNS_SERVERS.toString());
      return this.FACTORY_DNS_SERVERS;
    }

    private String[] getServersMethodConnectivityManager()
    {
      if (Build.VERSION.SDK_INT >= 21)
      {
        try
        {
          ArrayList priorityServersArrayList = new ArrayList();
          ArrayList serversArrayList = new ArrayList();

          ConnectivityManager connectivityManager = (ConnectivityManager)TaifunWiFi.this.context.getSystemService("connectivity");
          if (connectivityManager != null)
          {
            for (Network network : connectivityManager.getAllNetworks())
            {
              NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
              if (networkInfo.isConnected())
              {
                LinkProperties linkProperties = connectivityManager.getLinkProperties(network);
                List dnsServersList = linkProperties.getDnsServers();

                if (linkPropertiesHasDefaultRoute(linkProperties))
                {
                  for (InetAddress element : dnsServersList)
                  {
                    String dnsHost = element.getHostAddress();
                    priorityServersArrayList.add(dnsHost);
                  }

                }
                else
                {
                  for (InetAddress element : dnsServersList)
                  {
                    String dnsHost = element.getHostAddress();
                    serversArrayList.add(dnsHost);
                  }

                }

              }

            }

          }

          if (priorityServersArrayList.isEmpty())
          {
            priorityServersArrayList.addAll(serversArrayList);
          }

          if (priorityServersArrayList.size() > 0)
          {
            return (String[])priorityServersArrayList.toArray(new String[0]);
          }

        }
        catch (Exception ex)
        {
          Log.d("DnsServersDetector", "Exception detecting DNS servers using ConnectivityManager method", ex);
        }

      }

      return null;
    }

    private String[] getServersMethodSystemProperties()
    {
      if (Build.VERSION.SDK_INT < 26)
      {
        String re1 = "^\\d+(\\.\\d+){3}$";
        String re2 = "^[0-9a-f]+(:[0-9a-f]*)+:[0-9a-f]+$";
        ArrayList serversArrayList = new ArrayList();
        try
        {
          Class SystemProperties = Class.forName("android.os.SystemProperties");

          Method method = SystemProperties.getMethod("get", new Class[] { String.class });
          String[] netdns = { "net.dns1", "net.dns2", "net.dns3", "net.dns4" };
          for (int i = 0; i < netdns.length; i++)
          {
            Object[] args = { netdns[i] };
            String v = (String)method.invoke(null, args);
            if ((v != null) && ((v.matches("^\\d+(\\.\\d+){3}$")) || (v.matches("^[0-9a-f]+(:[0-9a-f]*)+:[0-9a-f]+$"))) && (!serversArrayList.contains(v))) {
              serversArrayList.add(v);
            }

          }

          if (serversArrayList.size() > 0)
          {
            return (String[])serversArrayList.toArray(new String[0]);
          }

        }
        catch (Exception ex)
        {
          Log.d("DnsServersDetector", "Exception detecting DNS servers using SystemProperties method", ex);
        }

      }

      return null;
    }

    private String[] getServersMethodExec()
    {
      if (Build.VERSION.SDK_INT >= 16)
      {
        try
        {
          Process process = Runtime.getRuntime().exec("getprop");
          InputStream inputStream = process.getInputStream();
          LineNumberReader lineNumberReader = new LineNumberReader(new InputStreamReader(inputStream));
          Set serversSet = methodExecParseProps(lineNumberReader);
          if ((serversSet != null) && (serversSet.size() > 0))
          {
            return (String[])serversSet.toArray(new String[0]);
          }

        }
        catch (Exception ex)
        {
          Log.d("DnsServersDetector", "Exception in getServersMethodExec", ex);
        }

      }

      return null;
    }

    private Set<String> methodExecParseProps(BufferedReader lineNumberReader)
      throws Exception
    {
      Set serversSet = new HashSet(10);
      String line;
      while ((line = lineNumberReader.readLine()) != null) {
        int split = line.indexOf("]: [");
        if (split != -1)
        {
          String property = line.substring(1, split);

          int valueStart = split + "]: [".length();
          int valueEnd = line.length() - 1;
          if (valueEnd < valueStart)
          {
            Log.d("DnsServersDetector", "Malformed property detected: \"" + line + '"');
          }
          else
          {
            String value = line.substring(valueStart, valueEnd);

            if (!value.isEmpty())
            {
              if ((property.endsWith(".dns")) || (property.endsWith(".dns1")) || 
                (property
                .endsWith(".dns2")) || 
                (property.endsWith(".dns3")) || 
                (property
                .endsWith(".dns4")))
              {
                InetAddress ip = InetAddress.getByName(value);
                if (ip != null) {
                  value = ip.getHostAddress();

                  if ((value != null) && 
                    (value.length() != 0))
                  {
                    serversSet.add(value);
                  }
                }
              }
            }
          }
        }
      }
      return serversSet;
    }

    @TargetApi(21)
    private boolean linkPropertiesHasDefaultRoute(LinkProperties linkProperties)
    {
      for (RouteInfo route : linkProperties.getRoutes()) {
        if (route.isDefaultRoute()) {
          return true;
        }
      }
      return false;
    }
  }
}
