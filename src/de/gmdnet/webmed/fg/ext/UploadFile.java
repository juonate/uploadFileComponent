/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gmdnet.webmed.fg.ext;

import de.gmdnet.webmed.fg.ExternalFgPanel;
import java.awt.FlowLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.params.HttpMethodParams;

/**
 *
 * @author Juan
 */
public class UploadFile extends ExternalFgPanel {

    private boolean valFlag = false;
    public JLabel label;
    private File targetFile;
    private JTextArea taTextResponse;
    private DefaultComboBoxModel cmbURLModel;
    private int count = 0;
    private int cantidad = 3;
    private boolean flagImpresion = true;
    private String Parent;
    private String idParent;
    private String Servlet;

    public UploadFile() {
        addTextFieldProperty("Parent", "Parent", null);
        addTextFieldProperty("idParent", "Id Parent", null);
        addTextFieldProperty("Servlet", "Servlet", null);

    }

    @Override
    public void setValue(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getValue() {
        Object o = new Object();
        return o;
    }

    public boolean setGadgetProperty(String key, String value) {
        if (key.equals("Parent")) {
            this.count += 1;
            this.Parent = value;
            startComponent(true);
            return true;
        }
        if (key.equals("idParent")) {
            this.count += 1;
            this.idParent = value;
            startComponent(true);
            return true;
        }
        if (key.equals("Servlet")) {
            this.count += 1;
            this.Servlet = value;
            startComponent(true);
            return true;
        }
        return false;
    }

    public void startComponent(boolean b) {
        if (this.count == this.cantidad) {
            imprimir("Start Component");

            JLabel lblTargetFile = new JLabel("Seleccione un archivo");

            final JTextField tfdTargetFile = new JTextField(30);
            tfdTargetFile.setEditable(false);

            final JButton btnDoUpload = new JButton("Subir");
            btnDoUpload.setEnabled(false);

            final JButton btnSelectFile = new JButton("Seleccione");
            btnSelectFile.addActionListener(
                    new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setFileHidingEnabled(false);
                    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    chooser.setMultiSelectionEnabled(false);
                    chooser.setDialogType(JFileChooser.OPEN_DIALOG);
                    chooser.setDialogTitle("Seleccione archivo...");

//                    chooser.showOpenDialog(label);
                    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        targetFile = chooser.getSelectedFile();
                        tfdTargetFile.setText(targetFile.toString());
                        btnDoUpload.setEnabled(true);

                    }
                }
            });

            btnDoUpload.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    String targetURL = Servlet;
                    try {
                        enviarParametros(Servlet,idParent, Parent);
                    } catch (MalformedURLException ex) {
                    } catch (IOException ex) {
                    }
                    PostMethod filePost = new PostMethod(targetURL);

                    filePost.getParams().setBooleanParameter(
                            HttpMethodParams.USE_EXPECT_CONTINUE,
                            false);

                    try {

                        imprimir("Subiendo " + targetFile.getName()
                                + " a " + targetURL);

                        Part[] parts = {
                            new FilePart(targetFile.getName(), targetFile)
                        };

                        filePost.setRequestEntity(
                                new MultipartRequestEntity(parts,
                                filePost.getParams()));

                        HttpClient client = new HttpClient();
                        client.getHttpConnectionManager().
                                getParams().setConnectionTimeout(5000);

                        int status = client.executeMethod(filePost);

                        if (status == HttpStatus.SC_OK) {
                            imprimir(
                                    "Subida completa, response="
                                    + filePost.getResponseBodyAsString());
                        } else {
                            imprimir(
                                    "Subida fallida, response="
                                    + HttpStatus.getStatusText(status));
                        }
                    } catch (Exception ex) {
                        imprimir("Error: " + ex.getMessage());
                        ex.printStackTrace();
                    } finally {
                        filePost.releaseConnection();
                    }

                }
            });

            setLayout(new FlowLayout());
            add(lblTargetFile);
            add(tfdTargetFile);
            add(btnSelectFile);
            add(btnDoUpload);
            setVisible(true);
        }
    }

    private void imprimir(String mensaje) {
        if (this.flagImpresion) {
            System.out.println(mensaje);
        }
    }

    private static void enviarParametros(String url, String idParent, String Parent) throws MalformedURLException, IOException {
        URL serverUrl = new URL(url);
        HttpURLConnection urlConnection = (HttpURLConnection) serverUrl.openConnection();

        urlConnection.setDoOutput(true);
        urlConnection.setRequestMethod("POST");

        BufferedWriter httpRequestBodyWriter = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
        httpRequestBodyWriter.write("idParent="+idParent+"&Parent="+Parent);
        httpRequestBodyWriter.close();

        Scanner httpResponseScanner = new Scanner(urlConnection.getInputStream());
        while (httpResponseScanner.hasNextLine()) {
            System.out.println(httpResponseScanner.nextLine());
        }
        httpResponseScanner.close();
    }
}
