package br.com.dominio.speedtest;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ReportarActivity extends AppCompatActivity {

    EditText txtNome;
    EditText txtemail;
    TextView txtDownload;
    TextView txtUpload;
    TextView txtSinal;
    TextView txtIP;
    Button btnEnviar;

    String user = "messias.junior@mdbrasil.com.br";
    String pass = "Junior10";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reportar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Reportando para o Suporte");
        actionBar.setDisplayHomeAsUpEnabled(true);

        String download = (String) getIntent().getExtras().get("download");
        String upload = (String) getIntent().getExtras().get("upload");
        String tipoSinal = (String) getIntent().getExtras().get("sinal");

        txtDownload = (TextView)findViewById(R.id.txt_download);
        txtDownload.setText(download);
        txtUpload = (TextView)findViewById(R.id.txt_upload);
        txtUpload.setText(upload);
        txtSinal = (TextView)findViewById(R.id.text_sinal);
        txtSinal.setText(tipoSinal);

        txtIP = (TextView)findViewById(R.id.id_end_ip);
        txtIP.setText("201.71.240.24");

        txtNome = (EditText)findViewById(R.id.id_nome);
        txtemail = (EditText)findViewById(R.id.id_email);

        btnEnviar = (Button)findViewById(R.id.id_enviar);
        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String to = txtemail.getText().toString();
                String subject = "Envio de teste de banda";
                String body = "Cliente: "+txtNome.getText().toString()+"\n Email: "+txtemail.getText().toString()+
                        "\n\n Download: "+txtDownload.getText().toString()+
                        "\n Upload: "+txtUpload.getText().toString()+
                        "\n Sinal: "+txtSinal.getText().toString()+
                        "\n End. IP: "+txtIP.getText().toString();
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_EMAIL, new String[] {to});
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                intent.putExtra(Intent.EXTRA_TEXT, body);

                intent.setType("message/rfc822");

                startActivity(Intent.createChooser(intent, "Selecione Email"));
            }
        });
    }
}
