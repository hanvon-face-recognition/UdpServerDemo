/* ----------------------------------------------------------
 * 文件名称：FXMLDocumentController.java
 * 
 * 作者：秦建辉
 * 
 * 微信：splashcn
 * 
 * 博客：http://www.firstsolver.com/wordpress/
 * 
 * 开发环境：
 *      NetBeans 8.1
 *      JDK 8u92
 *      
 * 版本历史：
 *      V1.1    2016年07月17日
 *              因SDK改进更新代码
 *
 *      V1.0    2014年09月09日
 *              接收心跳包数据
------------------------------------------------------------ */

package Splash;

import Com.FirstSolver.Splash.IDgramPacketHandler;
import Com.FirstSolver.Splash.UdpClientPlus;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 *
 * @author 秦建辉
 */
public class FXMLDocumentController implements Initializable, IDgramPacketHandler {
    
    private final String DeviceCharset = "GBK";
    
    private boolean IsServerRunning = false;
    
    public UdpClientPlus UdpServer = null;
       
    @FXML
    private ComboBox comboBoxServerIP;
    
    @FXML
    private TextField textFieldServerPort;
    
    @FXML
    private Button buttonStartListener;
    
    @FXML
    private CheckBox checkBoxTaskRequest;
    
    @FXML
    private TextArea textAreaAnswer;
        
    @FXML
    private void handleButtonClearAction(ActionEvent event) {
        textAreaAnswer.clear();
    }
    
    @FXML
    private void handleButtonStartListenerAction(ActionEvent event) throws IOException, Exception {
        if(IsServerRunning)
        {
            if(UdpServer != null)
            {
                UdpServer.close();
                UdpServer = null;
            }
            IsServerRunning = false;
            buttonStartListener.setText("开启侦听");
        }
        else
        {
            // 创建侦听服务器
            UdpServer = new UdpClientPlus(Integer.parseInt(textFieldServerPort.getText()), InetAddress.getByName(comboBoxServerIP.getValue().toString()));
            
            // 设置通信线程任务委托
            UdpServer.DgramPacketHandler = this;
             
            // 设置通信字符集
            UdpServer.CharsetName = DeviceCharset;
            
            // 开启侦听服务
            UdpServer.StartListenThread(null, 0);
            
            IsServerRunning = true;
            buttonStartListener.setText("停止侦听");            
        }
    }
    
    @Override
    public void OnDgramPacketReceived(InetAddress address, int port, byte[] content) throws Exception
    {
        // 接收字节数据
    }

    @Override
    public void OnDgramPacketReceived(InetAddress address, int port, String content) throws Exception {
        String message = "来自：" + address.getHostName() + " 内容：" + content + "\r\n";
        Platform.runLater(() -> {
            textAreaAnswer.appendText(message);
            if (checkBoxTaskRequest.isSelected())
            {
                try 
                {
                    UdpServer.Send("PostRequest()", DeviceCharset, address, port);
                } 
                catch (Exception ex)
                {
                    
                }
            }
        });
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) { 
        // 设置服务器地址
        try
        {
            List<String> IPList = new LinkedList<>();
            Enumeration<NetworkInterface> InterfaceList = NetworkInterface.getNetworkInterfaces();
            while (InterfaceList.hasMoreElements())
            { 
                NetworkInterface iFace = InterfaceList.nextElement();
                if(iFace.isLoopback() || iFace.isVirtual() || iFace.isPointToPoint() || !iFace.isUp()) continue;
                                
                Enumeration<InetAddress> AddrList = iFace.getInetAddresses(); 
                while (AddrList.hasMoreElements())
                { 
                    InetAddress address = AddrList.nextElement(); 
                    if ((address instanceof Inet4Address) || (address instanceof Inet6Address))
                    {
                        IPList.add(address.getHostAddress());                
                    }
                } 
            }
            
            if(!IPList.isEmpty())
            {
                comboBoxServerIP.setItems(FXCollections.observableList(IPList));
                comboBoxServerIP.setValue(IPList.get(0));
            }            
        }
        catch (SocketException ex) 
        {
            // 异常处理
        }
    }    
}
