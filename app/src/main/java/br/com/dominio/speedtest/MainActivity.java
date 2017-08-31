package br.com.dominio.speedtest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.anastr.speedviewlib.SpeedView;

import java.math.BigDecimal;
import java.util.List;

import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.ISpeedTestListener;
import fr.bmartel.speedtest.model.SpeedTestError;
import fr.bmartel.speedtest.model.UploadStorageType;

public class MainActivity extends AppCompatActivity {

    private static final BigDecimal VALOR_DE_MEGA_POR_SEGUNDO = new BigDecimal(1000000);
    /**
     * uri do servidor.
     */
    private final static String SPEED_TEST_SERVER_URI_DL = "http://mdbrasil.com.br/500m.dat";
    private final static String SPEED_TEST_SERVER_URI_UL = "http://mdbrasil.com.br/";
    /**
     * duracao setado para 15s.
     */
    private static final int SPEED_TEST_DURATION = 15000;
    /**
     * quantidade de tempo entre cada relatório de teste de velocidade definido para 5s.
     */
    private static final int REPORT_INTERVAL = 500;
    /**
     * timeout do socket setado para 3s.
     */
    private static final int SOCKET_TIMEOUT = 3000;
    /**
     * tamanho do arquivo do upload eh de 100M.
     */
    private static final int FILE_SIZE = 10000000;

    /**
     * socket
     */
    private static SpeedTestSocket speedTestSocket = new SpeedTestSocket();

    SpeedView speedometer;

    TextView lblTipoRede;
    String stringTipoRede;
    TextView lblSinal;
    String stringSinal;


    TextView lblDownload;
    double download;
    TextView lblUpload;
    double upload;
    double velocidade;
    double tempoDownload;
    double tempoUpload;
    double tempoTotal;

    boolean isDownload;
    boolean isUpload;

    ProgressBar progressBar;
    Button btnInicio;
    Button btnReportar;
    boolean isTerminado;
    Context context;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ConnectivityManager (Detecta Wifi, Mobile, Network)
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isWifiConn = networkInfo.isConnected();
        networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean isMobileConn = networkInfo.isConnected();

        lblSinal = (TextView)findViewById(R.id.id_sinal);

        if(isWifiConn) {
            stringTipoRede = "Conexão Wifi";
            @SuppressLint("WifiManagerLeak")
            WifiManager wifiManager = (WifiManager)getApplication().getSystemService(context.WIFI_SERVICE);
            List<ScanResult> scanResult = wifiManager.getScanResults();
            for(int i = 0; i < scanResult.size(); i++){
                int a = scanResult.get(i).level;
                stringSinal = Integer.toString(a);
                lblSinal.setText(stringSinal+"dbm");
            }
        }
        else if(isMobileConn) {
            stringTipoRede = "Conexão 3G/4G";
            lblSinal.setText("");
        }
        else {
            stringTipoRede = "Sem Conexão";
            lblSinal.setText("");
        }

        speedometer = (SpeedView) findViewById(R.id.speedView);

        lblTipoRede = (TextView)findViewById(R.id.id_tipo_conexao);
        lblTipoRede.setText(stringTipoRede);

        lblDownload = (TextView)findViewById(R.id.id_download);
        lblUpload = (TextView)findViewById(R.id.id_upload);

        progressBar = (ProgressBar)findViewById(R.id.id_progress_bar);
        progressBar.setScaleX(3f);
        progressBar.setScaleY(4f);

        lblDownload.setText("");
        lblUpload.setText("");

        btnInicio = (Button)findViewById(R.id.id_btn_iniciar);
        btnInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lblUpload.setText("");
                lblDownload.setText("");

                new SpeedTestTask().execute();

                isTerminado = false;
                btnInicio.setEnabled(false);
                btnInicio.setText("Testando...");
            }
        });

        btnReportar = (Button)findViewById(R.id.id_btn_relatar);
        btnReportar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ReportarActivity.class);

                if((lblDownload.getText().length() >0) && (lblUpload.getText().length() >0)) {
                    intent.putExtra("download", lblDownload.getText());
                    intent.putExtra("upload", lblUpload.getText());
                    intent.putExtra("sinal", lblSinal.getText());
                    startActivity(intent);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setIcon(R.drawable.attention);
                    builder.setMessage("Realize o teste antes de reportar");
                    builder.setNegativeButton("Aceitar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.setIcon(R.drawable.attention);
                    dialog.show();
                }
            }
        });
    }


    // AsyncTask
    class SpeedTestTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {

            speedTestSocket.setSocketTimeout(SOCKET_TIMEOUT);
            speedTestSocket.setUploadStorageType(UploadStorageType.FILE_STORAGE);

            speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {
                @Override
                public void onCompletion(SpeedTestReport report) {
                    if(report.getSpeedTestMode().toString() == "DOWNLOAD") {
                        download = report.getTransferRateBit().divide(VALOR_DE_MEGA_POR_SEGUNDO).doubleValue();
                        velocidade = report.getTransferRateBit().divide(VALOR_DE_MEGA_POR_SEGUNDO).doubleValue();
                        tempoDownload = ((double) report.getReportTime() - (double) report.getStartTime()) / 1000;
                        tempoTotal = tempoUpload + tempoDownload;
                        publishProgress(String.format("%.2f mbps", download), String.format(velocidade+""),
                                tempoDownload +" seg", String.format("%.2f seg", tempoTotal), "100");
                        speedTestSocket.startFixedUpload(SPEED_TEST_SERVER_URI_UL, FILE_SIZE, SPEED_TEST_DURATION, REPORT_INTERVAL);
                    } else if (report.getSpeedTestMode().toString() == "UPLOAD") {

                        upload = report.getTransferRateBit().divide(VALOR_DE_MEGA_POR_SEGUNDO).doubleValue();
                        ;
                        velocidade = report.getTransferRateBit().divide(VALOR_DE_MEGA_POR_SEGUNDO).doubleValue();
                        tempoUpload = ((double) report.getReportTime() - (double) report.getStartTime()) / 1000;
                        tempoTotal = tempoUpload - tempoUpload;
                        publishProgress(String.format("%.2f mbps", upload), String.format(velocidade+""),
                                tempoUpload +" seg", String.format("%.2f seg", tempoTotal),100 + "");
                        isTerminado = true;
                    }

                }

                @Override
                public void onProgress(float percent, SpeedTestReport report) {
                    if(report.getSpeedTestMode().toString() == "DOWNLOAD") {
                        isDownload = true;
                        isUpload = false;
                        download = report.getTransferRateBit().divide(VALOR_DE_MEGA_POR_SEGUNDO).doubleValue();
                        velocidade = report.getTransferRateBit().divide(VALOR_DE_MEGA_POR_SEGUNDO).doubleValue();
                        tempoDownload = ((double)report.getReportTime() - (double)report.getStartTime())/1000;
                        tempoTotal = tempoUpload + tempoDownload;
                        publishProgress(String.format("%.2f mbps", download), String.format(velocidade+""),
                                tempoDownload +" seg", String.format("%.2f seg", tempoTotal), (int)percent + "");
                    } else if (report.getSpeedTestMode().toString() == "UPLOAD") {
                        isDownload = false;
                        isUpload = true;
                        upload = report.getTransferRateBit().divide(VALOR_DE_MEGA_POR_SEGUNDO).doubleValue();;
                        velocidade = report.getTransferRateBit().divide(VALOR_DE_MEGA_POR_SEGUNDO).doubleValue();
                        tempoUpload = ((double)report.getReportTime() - (double)report.getStartTime())/1000;
                        tempoTotal = tempoUpload - tempoUpload;
                        publishProgress(String.format("%.2f mbps", upload), String.format(velocidade+""),
                                tempoUpload +" seg", String.format("%.2f seg", tempoTotal), (int)percent + "");

                    }
                }

                @Override
                public void onError(SpeedTestError speedTestError, String errorMessage) {

                }
            });
            speedTestSocket.startFixedDownload(SPEED_TEST_SERVER_URI_DL, SPEED_TEST_DURATION, REPORT_INTERVAL);
            return null;

        }
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            if (isDownload) {
                lblDownload.setText(values[0]);
                speedometer.speedTo(Float.parseFloat(values[1]), REPORT_INTERVAL);
                progressBar.setProgress(Integer.parseInt(values[4]));

            } else if (isUpload) {
                lblUpload.setText(values[0]);
                speedometer.speedTo(Float.parseFloat(values[1]), REPORT_INTERVAL);
                progressBar.setProgress(Integer.parseInt(values[4]));
            }

            if (isTerminado) {
                btnInicio.setText("Repetir teste");
                speedometer.speedTo(0);
                btnInicio.setEnabled(true);
            }
        }
    }
}
