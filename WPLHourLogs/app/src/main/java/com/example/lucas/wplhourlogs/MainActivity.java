/**************************************************************************************************
 * Project Name: WPL Hour Logs
 * @author Lucas Varella
 * @date 8/22/16
 * @Version 1.0
 *
 * Introduction:
 *************************************************************************************************/

package com.example.lucas.wplhourlogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity {

    // Item IDs for Database
    public static class ItemIDs {
        final static int MINUTES_ID = 1;
        final static int DESCRIPTION_ID = 2;
    }

    // file format items
    final static String VOLUNTEERS_FILE = "volunteerInfo.txt";
    final static String DESCRIPTIONS_FILE = "descriptionsAvailable.txt";
    final static String KEYWORDS_FILE = "keywords.txt";
    final static String STATE_SAVE_FILE = "stateSave.txt";
    final static String TEMP_FILE = "tempFile.txt";
    final static String BULK_FILE = "bulkFile.txt";
    final static String ADMIN_FILE = "adminFile.txt";
    final static String BLUETOOTH_FOLDER_PATH = "/sdcard/bluetooth/";
    final static String FILE_SEPARATOR = "~";
    final static String FORM_SEPARATOR = "||";
    final static String ITEM_SEPARATOR = "|";
    final static String SUBITEM_SEPARATOR = ",";   // separates items from their IDs
    static int archivedFiles = 0;

    // User Layout
    AutoCompleteTextView txtName;
    AutoCompleteTextView txtDescription;
    EditText txtDate;
    EditText txtHours;
    EditText txtMins;
    ImageButton btnSetDate;
    Button btnToday;
    Button addHour;
    Button subHour;
    Button addMin;
    Button subMin;
    Button btnSubmit;
    Button btnAdmin;

    // Password Layout
    EditText txtPassword;
    Button btnBack;
    Button btnGo;

    // Change Password Layout
    EditText txtNewPassword;
    EditText txtConfirmPassword;
    Button btnCancel;
    Button btnChangePassword;

    // Admin Layout
    // Instead of populating a ListView, multiple layouts were set up since content is static.
    // Each layout behaves as a button.
    RelativeLayout TransferLayout;
    RelativeLayout ReceiveNamesLayout;
    RelativeLayout ReceiveDescriptionsLayout;
    RelativeLayout ReceiveKeywordsLayout;
    RelativeLayout EditNamesLayout;
    RelativeLayout EditDescriptionsLayout;
    RelativeLayout EditKeywordsLayout;
    RelativeLayout ChangePasswordLayout;

    // Edit Layout
    EditText txtAdd;
    ListView listView;
    Button btnAdd;
    Button btnRemove;

    // Necessary items for name autocomplete
    ArrayList volunteers;
    static String[] names;
    ArrayAdapter<String> txtNameAdapter;
    // Necessary items for description autocomplete
    ArrayList availableDescriptions;
    static String[] descriptions;
    ArrayAdapter<String> txtDescriptionAdapter;
    // Necessary items fo keyword check
    ArrayList keywordList;
    static String[] keywords;
    // Necessary object for Edit Layout autocomplete
    ArrayAdapter<String> listViewAdapter;
    static int selectedItem;
    // Necessary object for date generation
    final SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
    // Secondary state save
    static String stateSave;
    // Alert Dialog items
    static String MESSAGE = "";
    static String POSITIVE_BUTTON = "";
    static String NEGATIVE_BUTTON = "";
    // constants that stand for the layout to switch to when orientation is changed.
    static int currentLayout = 0;
    final static int USER_LAYOUT = 0;
    final static int PASSWORD_LAYOUT = 1;
    final static int ADMIN_LAYOUT = 2;
    final static int EDIT_LAYOUT = 3;
    final static int CHANGE_PASSWORD_LAYOUT = 4;
    // constants that stand for the admin actions
    int actionRequested = -1;
    final static int TRANSFER_FORMS = 0;
    final static int TRANSFER_ALL_ARCHIVES = 1;
    final static int RECEIVE_NAMES = 2;
    final static int RECEIVE_DESCRIPTIONS = 3;
    final static int RECEIVE_KEYWORDS = 4;
    final static int CHANGE_PASSWORD = 5;
    final static int DELETE_ALL_ARCHIVES = 6;
    // The same layout is used for editing names, descriptions and keywords
    static int editStartValue;
    final static int EDIT_NAMES = 0;
    final static int EDIT_DESCRIPTIONS = 1;
    final static int EDIT_KEYWORDS = 2;
    // Other admin items
    static boolean isAdmin = false;
    static boolean isEditing = false;
    static int password = 123;
    // A helper variable to temporarily store the contents of a file
    static String content;
    // The PC from which to receive and which to send
    final static String PC_COMPANION = "TEENL-PC";
    // item used to hide keyboard
    InputMethodManager imm;
    // Necessary items for data transfer
    final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    static BluetoothDevice device = null;
    Intent intent = new Intent();
    static boolean justTransferred = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        InitializeAutoComplete();
        InitializeArchiveSystem();
        InitializeAdminOptions();
        InitializeKeywordSystem();

        imm  = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        InitializeStateSave();
    }

    public void SetUserLayout() {
        setContentView(R.layout.activity_main);
        currentLayout = USER_LAYOUT;

        txtDate = (EditText) findViewById(R.id.txtDate);
        txtHours = (EditText) findViewById(R.id.txtHours);
        txtMins = (EditText) findViewById(R.id.txtMins);

        txtDescription = (AutoCompleteTextView) findViewById(R.id.txtDescription);
        txtDescription.setThreshold(1);
        if (!availableDescriptions.isEmpty())
            txtDescriptionAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_singlechoice, descriptions);
        txtDescription.setAdapter(txtDescriptionAdapter);

        txtName = (AutoCompleteTextView) findViewById(R.id.txtName);
        txtName.setThreshold(1);
        if (!volunteers.isEmpty())
            txtNameAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_singlechoice, names);
        txtName.setAdapter(txtNameAdapter);

        btnToday = (Button) findViewById(R.id.btnToday);
        btnToday.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Date today = new Date(System.currentTimeMillis());
                txtDate.setText(df.format(today));
            }
        });

        addHour = (Button) findViewById(R.id.addHour);
        addHour.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int num = Integer.parseInt(txtHours.getText().toString());
                num += 1;
                txtHours.setText(String.valueOf(num));
            }
        });

        subHour = (Button) findViewById(R.id.subHour);
        subHour.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int num;
                if (!txtHours.getText().toString().equals("0")) {
                    num = Integer.parseInt(txtHours.getText().toString());
                    num -= 1;
                    txtHours.setText(String.valueOf(num));
                }
            }
        });

        addMin = (Button) findViewById(R.id.addMin);
        addMin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int num = 0;
                if (txtMins.getText().toString().equals("0")) num = 30;
                else {
                    int hours = Integer.parseInt(txtHours.getText().toString());
                    hours += 1;
                    txtHours.setText(String.valueOf(hours));
                }
                txtMins.setText(String.valueOf(num));
            }
        });

        subMin = (Button) findViewById(R.id.subMin);
        subMin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int num = 0;
                if (txtMins.getText().toString().equals("0")) {
                    int hours = Integer.parseInt(txtHours.getText().toString());
                    if (hours > 0) {
                        hours -= 1;
                        num = 30;
                    } else {
                        hours = 0;
                        num = 0;
                    }
                    txtHours.setText(String.valueOf(hours));
                }
                txtMins.setText(String.valueOf(num));
            }
        });

        btnSetDate = (ImageButton) findViewById(R.id.btnSetDate);
        btnSetDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DialogFragment dialog = new DatePickerFragment();
                dialog.show(getFragmentManager(), "datePicker");
            }
        });

        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (CheckReady()) {
                    CheckNewEntry();
                    if (justTransferred) {
                        ArchiveCurrentFile();
                        justTransferred = false;
                    }
                    if (SaveForm()) {
                        showAlertDialog("FORM SAVED!", "OK!", null);
                        ClearForm();
                    }
                }
            }
        });

        btnAdmin = (Button) findViewById(R.id.btnAdmin);
        btnAdmin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(txtName.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(txtDate.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(txtHours.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(txtMins.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(txtDescription.getWindowToken(), 0);
                SetPasswordLayout();
            }
        });
    }

    public void SetPasswordLayout() {
        setContentView(R.layout.password_dialog);
        currentLayout = PASSWORD_LAYOUT;
        isAdmin = true;
        final int actionRequested = this.actionRequested;

        txtPassword = (EditText) findViewById(R.id.txtPassword);

        btnBack = (Button) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(txtPassword.getWindowToken(), 0);
                if (actionRequested == CHANGE_PASSWORD) SetAdministratorLayout();
                else SetUserLayout();
            }
        });

        btnGo = (Button) findViewById(R.id.btnGo);
        btnGo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String response = txtPassword.getText().toString();
                if (response.isEmpty()) {
                    showAlertDialog("Incorrect Password.", "OK", null);
                }
                else if (Integer.parseInt(response) == password) {
                    imm.hideSoftInputFromWindow(txtPassword.getWindowToken(), 0);
                    SetAdministratorLayout();
                }
                else {
                    showAlertDialog("Incorrect Password.", "OK", null);
                    txtPassword.setText("");
                }
            }
        });
    }

    public void SetChangePasswordLayout() {
        setContentView(R.layout.change_password);
        currentLayout = CHANGE_PASSWORD_LAYOUT;

        txtNewPassword = (EditText) findViewById(R.id.txtNewPassword);
        txtConfirmPassword = (EditText) findViewById(R.id.txtConfirmPassword);

        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(txtNewPassword.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(txtConfirmPassword.getWindowToken(), 0);
                SetAdministratorLayout();
            }
        });

        btnChangePassword = (Button) findViewById(R.id.btnGo);
        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String response1 = txtNewPassword.getText().toString();
                String response2 = txtConfirmPassword.getText().toString();
                if (response1.isEmpty()) {
                    showAlertDialog("Please input a new password.", "OK", null);
                }
                else if (response2.isEmpty()) {
                    showAlertDialog("Please confirm your new password.", "OK", null);
                }
                else if (Integer.parseInt(response1) == Integer.parseInt(response2)) {
                    imm.hideSoftInputFromWindow(txtNewPassword.getWindowToken(), 0);
                    imm.hideSoftInputFromWindow(txtConfirmPassword.getWindowToken(), 0);
                    password = Integer.parseInt(response1);

                    try {
                        File file = new File(getFilesDir().getAbsolutePath(), ADMIN_FILE);
                        if (!file.exists()) {
                            file.createNewFile();
                        }

                        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                        writer.write(response1);
                        writer.close();

                    }
                    catch (IOException e) {
                        Toast.makeText(getApplicationContext(), "There has been an I/O issue! Get Help!", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                    showAlertDialog("SUCCESS!", "OK", null);
                    SetAdministratorLayout();
                }
                else {
                    showAlertDialog("Passwords don't match.", "OK", null);
                }
            }
        });
    }

    public void SetAdministratorLayout() {
        setContentView(R.layout.activity_admin);
        currentLayout = ADMIN_LAYOUT;

        TransferLayout = (RelativeLayout) findViewById(R.id.TransferLayout);
        TransferLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                actionRequested = TRANSFER_FORMS;
                showAlertDialog("Are you sure you want to transfer?", "YES", "Cancel");
            }
        });
        TransferLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                actionRequested = TRANSFER_ALL_ARCHIVES;
                showAlertDialog("Transfer all archived files?", "YES", "Cancel");
                return true;
            }
        });
        ReceiveNamesLayout = (RelativeLayout) findViewById(R.id.ReceiveNamesLayout);
        ReceiveNamesLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                actionRequested = RECEIVE_NAMES;
                showAlertDialog("Receive names file?", "YES", "Cancel");
            }
        });
        ReceiveDescriptionsLayout = (RelativeLayout) findViewById(R.id.ReceiveDescriptionsLayout);
        ReceiveDescriptionsLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                actionRequested = RECEIVE_DESCRIPTIONS;
                showAlertDialog("Receive descriptions file?", "YES", "Cancel");
            }
        });
        ReceiveKeywordsLayout = (RelativeLayout) findViewById(R.id.ReceiveKeywordsLayout);
        ReceiveKeywordsLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                actionRequested = RECEIVE_KEYWORDS;
                showAlertDialog("Receive keywords file?", "YES", "Cancel");
            }
        });
        EditNamesLayout = (RelativeLayout) findViewById(R.id.EditNamesLayout);
        EditNamesLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editStartValue = EDIT_NAMES;
                SetEditLayout();
            }
        });
        EditDescriptionsLayout = (RelativeLayout) findViewById(R.id.EditDescriptionsLayout);
        EditDescriptionsLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editStartValue = EDIT_DESCRIPTIONS;
                SetEditLayout();
            }
        });
        EditKeywordsLayout = (RelativeLayout) findViewById(R.id.EditKeywordsLayout);
        EditKeywordsLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editStartValue = EDIT_KEYWORDS;
                SetEditLayout();
            }
        });
        ChangePasswordLayout = (RelativeLayout) findViewById(R.id.ChangePasswordLayout);
        ChangePasswordLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                actionRequested = CHANGE_PASSWORD;
                showAlertDialog("Change Admin Password?", "YES", "Cancel");
            }
        });
        ChangePasswordLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                actionRequested = DELETE_ALL_ARCHIVES;
                showAlertDialog("Delete all archived files?", "YES", "Cancel");
                return true;
            }
        });
    }

    public void SetEditLayout() {
        setContentView(R.layout.activity_edit);
        currentLayout = EDIT_LAYOUT;
        isEditing = true;
        selectedItem = -1;

        txtAdd = (EditText) findViewById(R.id.txtAdd);

        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedItem = position;
            }
        });
        switch (editStartValue) {
            case EDIT_NAMES:
                if (!volunteers.isEmpty()) listViewAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, names);
                listView.setAdapter(listViewAdapter);
                break;
            case EDIT_DESCRIPTIONS:
                if (!availableDescriptions.isEmpty()) listViewAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, descriptions);
                listView.setAdapter(listViewAdapter);
                break;
            case EDIT_KEYWORDS:
                if (!keywordList.isEmpty()) listViewAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, keywords);
                listView.setAdapter(listViewAdapter);
                break;
        }

        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean proceed = true;
                String test;
                String input = txtAdd.getText().toString();

                switch (editStartValue) {
                    case EDIT_NAMES:
                        if ((input.isEmpty()) || (input.equals(" ")) || (input.length() < 4)) {
                            showAlertDialog("Please enter a valid name.", "OK", null);
                            proceed = false;
                        }
                        if (!input.contains(" ")) {
                            showAlertDialog("Please enter a full name.", "OK", null);
                            proceed = false;
                        }
                        test = input.toLowerCase();
                        for (int i = 0; i < test.length(); i++) {
                            char c = test.charAt(i);
                            if (c < 94 || c > 122) {
                                switch (c) {
                                    case 39:
                                    case 40:
                                    case 41:
                                    case 43:
                                    case 46:
                                    case 45:
                                    case 63:
                                    case 33:
                                    case 32:
                                        break;
                                    default:
                                        showAlertDialog("Character " + Character.toString(c) + " is invalid!", "OK", null);
                                        proceed = false;
                                }
                            }
                        }

                        if (proceed) {
                            txtAdd.setText("");
                            volunteers.add(input);
                            names = new String[volunteers.size()];
                            int i = 0;
                            for (Object volunteer : volunteers) {
                                names[i] = volunteer.toString();
                                i++;
                            }
                            try {
                                File file = new File(getFilesDir().getAbsolutePath(), VOLUNTEERS_FILE);
                                if (!file.exists()) {
                                    file.createNewFile();
                                } else {
                                    file.delete();
                                    file.createNewFile();
                                }
                                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                                for (String name : names) {
                                    writer.append(name);
                                    writer.newLine();
                                }
                                writer.close();
                            } catch (IOException e) {
                                Toast.makeText(getApplicationContext(), "There has been an I/O issue! (get help!)", Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                            FillList(names);
                        }
                        break;

                    case EDIT_DESCRIPTIONS:
                        if ((input.isEmpty()) || (input.equals(" ")) || (input.length() < 3)) {
                            showAlertDialog("Please enter a valid input (at least 3 characters).", "OK", null);
                            proceed = false;
                        }
                        test = input.toLowerCase();
                        for (int i = 0; i < test.length(); i++) {
                            char c = test.charAt(i);
                            if (c < 94 || c > 122) {
                                switch (c) {
                                    case 39:
                                    case 40:
                                    case 41:
                                    case 43:
                                    case 46:
                                    case 45:
                                    case 63:
                                    case 33:
                                    case 32:
                                        break;
                                    default:
                                        showAlertDialog("Character " + Character.toString(c) + " is invalid!", "OK", null);
                                        proceed = false;
                                }
                            }
                        }

                        if (proceed) {
                            txtAdd.setText("");
                            availableDescriptions.add(input);
                            descriptions = new String[availableDescriptions.size()];
                            int i = 0;
                            for (Object description : availableDescriptions) {
                                descriptions[i] = description.toString();
                                i++;
                            }
                            try {
                                File file = new File(getFilesDir().getAbsolutePath(), DESCRIPTIONS_FILE);
                                if (!file.exists()) {
                                    file.createNewFile();
                                } else {
                                    file.delete();
                                    file.createNewFile();
                                }
                                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                                for (String description : descriptions) {
                                    writer.append(description);
                                    writer.newLine();
                                }
                                writer.close();
                            } catch (IOException e) {
                                Toast.makeText(getApplicationContext(), "There has been an I/O issue! (get help!)", Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                            FillList(descriptions);

                        }
                        break;

                    case EDIT_KEYWORDS:
                        if ((input.isEmpty()) || (input.equals(" ")) || (input.length() < 3)) {
                            showAlertDialog("Please enter a valid input (at least 3 characters).", "OK", null);
                            proceed = false;
                        }
                        test = input.toLowerCase();
                        for (int i = 0; i < test.length(); i++) {
                            char c = test.charAt(i);
                            if (c < 94 || c > 122) {
                                switch (c) {
                                    case 39:
                                    case 40:
                                    case 41:
                                    case 43:
                                    case 46:
                                    case 45:
                                    case 63:
                                    case 33:
                                    case 32:
                                        break;
                                    default:
                                        showAlertDialog("Character " + Character.toString(c) + " is invalid!", "OK", null);
                                        proceed = false;
                                }
                            }
                        }

                        if (proceed) {
                            txtAdd.setText("");
                            keywordList.add(input);
                            keywords = new String[keywordList.size()];
                            int i = 0;
                            for (Object description : keywordList) {
                                keywords[i] = description.toString();
                                i++;
                            }
                            try {
                                File file = new File(getFilesDir().getAbsolutePath(), KEYWORDS_FILE);
                                if (!file.exists()) {
                                    file.createNewFile();
                                } else {
                                    file.delete();
                                    file.createNewFile();
                                }
                                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                                for (String keyword : keywords) {
                                    writer.append(keyword);
                                    writer.newLine();
                                }
                                writer.close();
                            } catch (IOException e) {
                                Toast.makeText(getApplicationContext(), "There has been an I/O issue! (get help!)", Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                            FillList(keywords);

                        }
                }

            }
        });

        btnRemove = (Button) findViewById(R.id.btnRemove);
        btnRemove.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (selectedItem == -1)
                    showAlertDialog("Please select an item to remove.", "OK", null);
                else {
                    int i;
                    switch (editStartValue) {
                        case EDIT_NAMES:
                            volunteers.remove(selectedItem);
                            names = new String[volunteers.size()];
                            i = 0;
                            for (Object volunteer : volunteers) {
                                names[i] = volunteer.toString();
                                i++;
                            }
                            try {
                                File file = new File(getFilesDir().getAbsolutePath(), VOLUNTEERS_FILE);
                                if (!file.exists()) {
                                    file.createNewFile();
                                } else {
                                    file.delete();
                                    file.createNewFile();
                                }
                                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                                for (String name : names) {
                                    writer.append(name);
                                    writer.newLine();
                                }
                                writer.close();
                            } catch (IOException e) {
                                Toast.makeText(getApplicationContext(), "There has been an I/O issue! (get help!)", Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                            FillList(names);
                            break;

                        case EDIT_DESCRIPTIONS:
                            availableDescriptions.remove(selectedItem);
                            descriptions = new String[availableDescriptions.size()];
                            i = 0;
                            for (Object description : availableDescriptions) {
                                descriptions[i] = description.toString();
                                i++;
                            }
                            try {
                                File file = new File(getFilesDir().getAbsolutePath(), DESCRIPTIONS_FILE);
                                if (!file.exists()) {
                                    file.createNewFile();
                                } else {
                                    file.delete();
                                    file.createNewFile();
                                }
                                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                                for (String description : descriptions) {
                                    writer.append(description);
                                    writer.newLine();
                                }
                                writer.close();
                            } catch (IOException e) {
                                Toast.makeText(getApplicationContext(), "There has been an I/O issue! (get help!)", Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                            FillList(descriptions);
                            break;

                        case EDIT_KEYWORDS:
                            keywordList.remove(selectedItem);
                            keywords = new String[keywordList.size()];
                            i = 0;
                            for (Object keyword : keywordList) {
                                keywords[i] = keyword.toString();
                                i++;
                            }
                            try {
                                File file = new File(getFilesDir().getAbsolutePath(), KEYWORDS_FILE);
                                if (!file.exists()) {
                                    file.createNewFile();
                                } else {
                                    file.delete();
                                    file.createNewFile();
                                }
                                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                                for (String keyword : keywords) {
                                    writer.append(keyword);
                                    writer.newLine();
                                }
                                writer.close();
                            } catch (IOException e) {
                                Toast.makeText(getApplicationContext(), "There has been an I/O issue! (get help!)", Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                            FillList(keywords);
                    }
                    selectedItem = -1;
                }
            }
        });

    }

    public void FillList(String[] content) {
        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedItem = position;
            }
        });
        listViewAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, content);
        listView.setAdapter(listViewAdapter);
    }

    public void InitializeArchiveSystem() {
        boolean done = false;
        int fileNum = 0;
        while (!done) {
            File file = new File(getFilesDir().getAbsolutePath(), "tempFile" + String.valueOf(fileNum) + ".txt");
            if (!file.exists()) done = true;
            else fileNum++;
        }
        archivedFiles = fileNum;
    }

    public void InitializeStateSave() {
        switch (currentLayout) {
            case USER_LAYOUT:
                SetUserLayout();
                try {
                    File file = new File(getFilesDir().getAbsolutePath(), STATE_SAVE_FILE);
                    if (!file.exists()) {
                        file.createNewFile();
                    } else {
                        String string = "";
                        String str;
                        BufferedReader reader = new BufferedReader(new FileReader(file));
                        while (!((str = reader.readLine()) == null)) { string += str; }
                        String[] items = string.split(",");
                        if (Integer.parseInt(items[0]) == 1) justTransferred = true;
                        if (!items[1].equals("_")) txtName.setText(items[1]);
                        if (items.length > 1) {
                            if (!items[2].equals("_")) txtHours.setText(items[2]);
                            if (!items[3].equals("_")) txtMins.setText(items[3]);
                            if (!items[4].equals("_")) txtDescription.setText(items[4]);

                        }
                        reader.close();
                    }
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "There has been an I/O issue! (get help!)", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                break;
            case PASSWORD_LAYOUT:
                SetPasswordLayout();
                txtPassword.setText(stateSave);
                break;
            case ADMIN_LAYOUT:
                SetAdministratorLayout();
                break;
            case EDIT_LAYOUT:
                SetEditLayout();
                txtAdd.setText(stateSave);
                listView.setAdapter(listViewAdapter);
                break;
            case CHANGE_PASSWORD_LAYOUT:
                SetChangePasswordLayout();
                String[] content = stateSave.split(ITEM_SEPARATOR);
                txtNewPassword.setText(content[0]);
                txtConfirmPassword.setText(content[1]);
        }
    }

    public void InitializeAutoComplete() {
        try {
            File file = new File(getFilesDir().getAbsolutePath(), VOLUNTEERS_FILE);
            if (!file.exists()) {
                file.createNewFile();
            }
            else {
                volunteers = new ArrayList();
                String str;
                BufferedReader reader = new BufferedReader(new FileReader(file));
                while (!((str = reader.readLine())== null)) { volunteers.add(str); }
                reader.close();
                if (!volunteers.isEmpty()) {
                    names = new String[volunteers.size()];
                    int i = 0;
                    for (Object volunteer : volunteers) {
                        names[i] = volunteer.toString();
                        i++;
                    }
                } // else do not use names!
            }
            file = new File(getFilesDir().getAbsolutePath(), DESCRIPTIONS_FILE);
            if (!file.exists()) {
                file.createNewFile();
            }
            else {
                availableDescriptions = new ArrayList();
                String str;
                BufferedReader reader = new BufferedReader(new FileReader(file));
                while (!((str = reader.readLine())== null)) { availableDescriptions.add(str); }
                if (!availableDescriptions.isEmpty()) {
                    descriptions = new String[availableDescriptions.size()];
                    int i = 0;
                    for (Object description : availableDescriptions) {
                        descriptions[i] = description.toString();
                        i++;
                    }
                } // else do not use descriptions!
                reader.close();
            }
        }
        catch (IOException e)
        {
            Toast.makeText(getApplicationContext(), "There has been an I/O issue! (get help!)", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public void InitializeAdminOptions() {
        try {
            File file = new File(getFilesDir().getAbsolutePath(), ADMIN_FILE);
            if (!file.exists()) {
                file.createNewFile();
            }
            else {
                String string = "";
                String str;
                BufferedReader reader = new BufferedReader(new FileReader(file));
                while (!((str = reader.readLine())== null)) { string += str; }
                if (!string.isEmpty()) password = Integer.parseInt(string);
                reader.close();
            }
        }
        catch (IOException e)
        {
            Toast.makeText(getApplicationContext(), "There has been an I/O issue! (get help!)", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public void InitializeKeywordSystem() {
        try {
            File file = new File(getFilesDir().getAbsolutePath(), KEYWORDS_FILE);
            if (!file.exists()) {
                file.createNewFile();
            }
            else {
                keywordList = new ArrayList();
                String str;
                BufferedReader reader = new BufferedReader(new FileReader(file));
                while (!((str = reader.readLine())== null)) { keywordList.add(str); }
                reader.close();
                if (!keywordList.isEmpty()) {
                    keywords = new String[keywordList.size()];
                    int i = 0;
                    for (Object keyword : keywordList) {
                        keywords[i] = keyword.toString();
                        i++;
                    }
                }
                // else do not use keywords!
            }
        }
        catch (IOException e)
        {
            Toast.makeText(getApplicationContext(), "There has been an I/O issue! (get help!)", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public void CheckNewEntry() {
        boolean newInput = true;
        String input = txtName.getText().toString();
        for (String name : names) {
            if (input.equals(name)) newInput = false;
        }
        if (newInput) {
            volunteers.add(input);
            names = new String[volunteers.size()];
            int i = 0;
            for (Object volunteer : volunteers) {
                names[i] = volunteer.toString();
                i++;
            }
            try {
                File file = new File(getFilesDir().getAbsolutePath(), VOLUNTEERS_FILE);
                if (!file.exists()) {
                    file.createNewFile();
                } else {
                    file.delete();
                    file.createNewFile();
                }
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                for (String Name : names) {
                    writer.append(Name);
                    writer.newLine();
                }
                writer.close();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "There has been an I/O issue! (get help!)", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            txtNameAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_singlechoice, names);
            txtName.setAdapter(txtNameAdapter);
        }

        newInput = true;
        input = txtDescription.getText().toString();
        if (descriptions != null)
            for (String description : descriptions) if (input.equals(description)) newInput = false;
        if (newInput) {
            availableDescriptions.add(input);
            descriptions = new String[availableDescriptions.size()];
            int i = 0;
            for (Object description : availableDescriptions) {
                descriptions[i] = description.toString();
                i++;
            }
            try {
                File file = new File(getFilesDir().getAbsolutePath(), DESCRIPTIONS_FILE);
                if (!file.exists()) {
                    file.createNewFile();
                } else {
                    file.delete();
                    file.createNewFile();
                }
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                for (String description : descriptions) {
                    writer.append(description);
                    writer.newLine();
                }
                writer.close();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "There has been an I/O issue! (get help!)", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            txtDescriptionAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_singlechoice, descriptions);
            txtDescription.setAdapter(txtDescriptionAdapter);
        }
    }

    public boolean CheckReady() {
        String name = txtName.getText().toString();
        if ((name.isEmpty())|| (name.equals(" ")) || (name.length() < 4)) {
            showAlertDialog("Please enter a valid name.", "OK", null);
            return false;
        }
        if (!name.contains(" ")) {
            showAlertDialog("Please enter your full name.", "OK", null);
            return false;
        }
        name = name.toLowerCase();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c < 94 || c > 122) {
                switch (c) {
                    case 39:
                    case 40:
                    case 41:
                    case 43:
                    case 46:
                    case 45:
                    case 63:
                    case 33:
                    case 32:
                        break;
                    default:
                        showAlertDialog("Character " + Character.toString(c) + " is invalid!", "OK", null);
                        return false;
                }
            }
        }
        for (int i = 0; i < keywords.length; i++) {
            if (name.contains(keywords[i].toLowerCase())) {
                int index = name.indexOf(keywords[i].toLowerCase());
                if (index == 0) {
                    if (name.charAt(index + keywords[i].length()) == ' ') {
                        showAlertDialog("'" + keywords[i].toLowerCase() + "' is not allowed.", "OK", null);
                        return false;
                    }
                } else if (index+keywords[i].length() == name.length()-1) {
                    if (name.charAt(index) == ' ') {
                        showAlertDialog("'" + keywords[i].toLowerCase() + "' is not allowed.", "OK", null);
                        return false;
                    }
                } else if (name.charAt(index-1) == ' ' && name.charAt(index+keywords[i].length()) == ' ') {
                    showAlertDialog("'" + keywords[i].toLowerCase() + "' is not allowed.", "OK", null);
                    return false;
                }
            }
        }

        String date = txtDate.getText().toString();
        if ((date.isEmpty())|| (date.equals(" ")) || (date.length() != 10)) {
            showAlertDialog("Please enter a valid date (MM/DD/YYYY format).", "OK", null);
            return false;
        }
        String[] items = date.split("/");
        if (items.length != 3) {
            showAlertDialog("Please enter a valid date (MM/DD/YYYY format).", "OK", null);
            return false;
        }
        else if (items[0].length() != 2) {
            showAlertDialog("Please enter a valid date (MM/DD/YYYY format).", "OK", null);
            return false;
        }
        else if (items[1].length() != 2) {
            showAlertDialog("Please enter a valid date (MM/DD/YYYY format).", "OK", null);
            return false;
        }
        else if (items[2].length() != 4) {
            showAlertDialog("Please enter a valid date (MM/DD/YYYY format).", "OK", null);
            return false;
        }

        int month = Integer.parseInt(items[0]);
        int day = Integer.parseInt(items[1]);
        int year = Integer.parseInt(items[2]);
        switch (month) {
            case 2:
                boolean leapYear = false;
                if (year%4 == 0) {
                    if (year%100 == 0) {
                        if (year%400 == 0) leapYear = true;
                    }
                    else leapYear = true;
                }
                if (leapYear) {
                    if (!(day > 0 && day <= 29)) {
                        showAlertDialog("Please enter a valid date.", "OK", null);
                        return false;
                    }
                }
                else {
                    if (!(day > 0 && day <= 28)) {
                        showAlertDialog("Please enter a valid date.", "OK", null);
                        return false;
                    }
                }
                break;
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                if (!(day > 0 && day <= 31)) {
                    showAlertDialog("Please enter a valid date.", "OK", null);
                    return false;
                }
                break;
            case 4:
            case 6:
            case 9:
            case 11:
                if (!(day > 0 && day <= 30)) {
                    showAlertDialog("Please enter a valid date.", "OK", null);
                    return false;
                }
                break;
            default:
                showAlertDialog("Please enter a valid date.", "OK", null);
                return false;

        }
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        if (!((year == currentYear - 2) || (year == currentYear - 1) || (year == currentYear))) {
            showAlertDialog("Please enter a valid year.", "OK", null);
            return false;
        }

        String hours = txtHours.getText().toString();
        String minutes = txtMins.getText().toString();
        if (minutes.isEmpty()) {
            txtMins.setText("0");
            minutes = "0";
        }
        if (hours.isEmpty()) {
            txtHours.setText("0");
            hours = "0";
        }
        if (txtHours.getText().toString().equals("0") && txtMins.getText().toString().equals("0")) {
            showAlertDialog("Please enter a valid number of hours.", "OK", null);
            return false;
        }
        int mins = Integer.parseInt(minutes);
        if (!(mins == 0 || mins == 30)) {
            if (mins < 30) txtMins.setText("30");
            else if (mins > 30) {
                txtMins.setText("0");
                txtHours.setText(Integer.toString(Integer.parseInt(hours) + 1));
            }
        }
        if (Integer.parseInt(hours) > 12) {
            showAlertDialog("Please enter a valid number of hours (no way you stayed here that long!).", "OK", null);
            return false;
        }

        String description = txtDescription.getText().toString();
        if ((description.isEmpty())|| (description.equals(" ")) || (description.length() < 3)) {
            showAlertDialog("Please enter a valid description (at least 3 characters).", "OK", null);
            return false;
        }
        description = description.toLowerCase();
        for (int i = 0; i < description.length(); i++) {
            char c = description.charAt(i);
            if (c < 94 || c > 122) {
                switch (c) {
                    case 39:
                    case 40:
                    case 41:
                    case 43:
                    case 46:
                    case 45:
                    case 63:
                    case 33:
                    case 32: break;
                    default: showAlertDialog("Character " + Character.toString(c) + " is invalid!", "OK", null);
                        return false;
                }
            }
        }
        for (int i = 0; i < keywords.length; i++) {
            if (description.contains(keywords[i].toLowerCase())) {
                showAlertDialog("'" + keywords[i].toLowerCase() + "' is not allowed.", "OK", null);
                return false;
            }
        }
        return true;
    }

    public String PrepareForm(String content) {
        // file format
        String form = content;
        if (!content.isEmpty()) form += FORM_SEPARATOR;
        String name = txtName.getText().toString();
        form += name + ITEM_SEPARATOR;
        form += txtDate.getText().toString() + ITEM_SEPARATOR;
        int minutes = (Integer.parseInt(txtHours.getText().toString()) * 60) + Integer.parseInt(txtMins.getText().toString());
        form += String.valueOf(minutes) + SUBITEM_SEPARATOR + ItemIDs.MINUTES_ID + ITEM_SEPARATOR;
        form += txtDescription.getText().toString() + SUBITEM_SEPARATOR + ItemIDs.DESCRIPTION_ID;
        return form;
    }

    public void ClearForm() {
        txtName.setText("");
        txtDate.setText("");
        txtHours.setText("0");
        txtMins.setText("0");
        txtDescription.setText("");
    }

    public boolean SaveForm()
    {
        try {
            File file = new File(getFilesDir().getAbsolutePath(), TEMP_FILE);
            if (!file.exists()) {
                file.createNewFile();
            }
            String content = "";
            String str;
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while (!((str = reader.readLine())== null)) content += str;
            reader.close();

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.append(PrepareForm(content));
            writer.close();

        }
        catch (IOException e) {
            Toast.makeText(getApplicationContext(), "There has been an I/O issue! FORM NOT SAVED.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        return true;
    }

    public void SaveState() {
        switch (currentLayout) {
            case USER_LAYOUT:
                try {
                    File file = new File(getFilesDir().getAbsolutePath(), STATE_SAVE_FILE);
                    if (!file.exists()) {
                       file.createNewFile();
                     }
                    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                    writer.write(PrepareState());
                    writer.close();
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "There has been an I/O issue! (get help!)", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                break;
            case PASSWORD_LAYOUT:
                stateSave = txtPassword.getText().toString();
                break;
            case ADMIN_LAYOUT:
                break;
            case EDIT_LAYOUT:
                stateSave = txtAdd.getText().toString();
                break;
            case CHANGE_PASSWORD_LAYOUT:
                stateSave = txtNewPassword.getText().toString() + ITEM_SEPARATOR + txtConfirmPassword.getText().toString();
        }
    }

    public String PrepareState() {
        String state = "";
        if (justTransferred) state += "1" + SUBITEM_SEPARATOR;
        else state += "0" + SUBITEM_SEPARATOR;
        if (txtName.getText().toString().isEmpty()) state += "_" + SUBITEM_SEPARATOR;
        else state += txtName.getText().toString() + SUBITEM_SEPARATOR;
        if (txtHours.getText().toString().isEmpty()) state += "_" + SUBITEM_SEPARATOR;
        else state += txtHours.getText().toString() + SUBITEM_SEPARATOR;
        if (txtMins.getText().toString().isEmpty()) state += "_" + SUBITEM_SEPARATOR;
        else state += txtMins.getText().toString() + SUBITEM_SEPARATOR;
        if (txtDescription.getText().toString().isEmpty()) state += "_";
        else state += txtDescription.getText().toString();
        return state;
    }

    public void ArchiveCurrentFile() {
        try {
            File file = new File(getFilesDir().getAbsolutePath(), TEMP_FILE);
            if (!file.exists()) {
                Toast.makeText(getApplicationContext(), "There has been an I/O issue! (get help!)", Toast.LENGTH_LONG).show();
            }
            String content = "";
            String str;
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while (!((str = reader.readLine())== null)) content += str;
            reader.close();
            file.delete();

            file = new File(getFilesDir().getAbsolutePath(), "tempFile" + archivedFiles + ".txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(content);
            writer.close();
            archivedFiles++;
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "There has been an I/O issue! (get help!)", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public void showAlertDialog(String message, String positive, String negative) {
        MESSAGE = message;
        POSITIVE_BUTTON = positive;
        NEGATIVE_BUTTON = negative;
        DialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "alertDialog");
    }

    public void PrepareToTransfer(String fileName)
    {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals(PC_COMPANION)) {
                    MainActivity.device = device;
                    Toast.makeText(this, "Attempting Transfer...", Toast.LENGTH_SHORT).show();
                    btTransfer(fileName);
                }
            }
        } else {
            Toast.makeText(this, "Please pair your computer to this device.", Toast.LENGTH_SHORT).show();
        }
    }

    public void PrepareFormTransfer() {
        try {
            File file = new File(getFilesDir().getAbsolutePath(), TEMP_FILE);
            if (!file.exists()) {
                Toast.makeText(getApplicationContext(), "There has been an I/O issue! (get help!)", Toast.LENGTH_LONG).show();
            }
            content = "";
            String str;
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while (!((str = reader.readLine())== null)) content += str;
            reader.close();
            file.delete();

            FileOutputStream fos = openFileOutput(TEMP_FILE, Context.MODE_WORLD_READABLE);
            fos.write(content.getBytes());
            fos.close();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "There has been an I/O issue! (get help!)", Toast.LENGTH_LONG).show();
        }
    }

    public void PrepareBulkTransfer() {
        try {
            File bulkFile = new File(getFilesDir().getAbsolutePath(), BULK_FILE);
            if (bulkFile.exists()) {
                bulkFile.delete();
            }
            File currentFile;
            String content = "";
            String str;
            BufferedReader reader;
            for (int i = 0; i < archivedFiles; i++) {
                currentFile = new File(getFilesDir().getAbsolutePath(), "tempFile" + i + ".txt");
                if (!currentFile.exists()) {
                    archivedFiles = i;
                    break;
                }
                if (i != 0) content += FILE_SEPARATOR;
                reader = new BufferedReader(new FileReader(currentFile));
                while (!((str = reader.readLine())== null)) content += str;
                reader.close();
            }

            FileOutputStream fos = openFileOutput(BULK_FILE, Context.MODE_WORLD_READABLE);
            fos.write(content.getBytes());
            fos.close();

        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "There has been an I/O issue! (get help!)", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public void btTransfer(String fileName){
        File file = getFileStreamPath(fileName);
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));

        PackageManager pm = getPackageManager();
        List appsList = pm.queryIntentActivities(intent, 0);
        if(appsList.size() > 0)
        {
            String packageName = null;
            String className = null;
            boolean found = false;
            for(int i = 0; i < appsList.size(); i++){
                ResolveInfo info = (ResolveInfo) appsList.get(i);
                packageName = info.activityInfo.packageName;
                if(packageName.equals("com.android.bluetooth")){
                    className = info.activityInfo.name;
                    found = true;
                    break;// found
                }
            }

            if(!found){
                Toast.makeText(this, "Not found!", Toast.LENGTH_SHORT).show();
            }
            else
            {
                intent.setClassName(packageName, className);
                startActivityForResult(intent, 1);
                justTransferred = true;
            }
        }
    }

    public void SaveTempFile() {
        try {
            File file = new File(getFilesDir().getAbsolutePath(), TEMP_FILE);
            if (!file.exists()) {
                file.createNewFile();
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "There has been an I/O issue! (get help!)", Toast.LENGTH_LONG).show();
        }
    }

    public void RetrieveComputerFile(String fileName) {
        try {
            File file = new File(BLUETOOTH_FOLDER_PATH, fileName);
            if (!file.exists()) {
                showAlertDialog("There is no received file. If that is not the case, get help!", "OK", null);
            } else {
                ArrayList<String> contents = new ArrayList<>();
                String str;
                BufferedReader reader = new BufferedReader(new FileReader(file));
                while (!((str = reader.readLine()) == null)) contents.add(str.trim());
                reader.close();
                file.delete();

                file = new File(getFilesDir().getAbsolutePath(), fileName);
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                for (int i = 0; i < contents.size(); i++) {
                    if (i > 0) writer.newLine();
                    writer.write(contents.get(i));
                }
                writer.close();
                InitializeAutoComplete();
                showAlertDialog("SUCCESS!", "OK", null);
            }
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "There has been an I/O issue! (get help!)", Toast.LENGTH_LONG).show();
        }
    }

    public void DeleteAllArchives() {
        try {
            for (int i = 0; i < archivedFiles; i++) {
                File file = new File(getFilesDir().getAbsolutePath(), "tempFile" + i + ".txt");
                if (file.exists()) if (!file.delete()) throw new IOException();
            }
            archivedFiles = 0;
            showAlertDialog("Archives Reset.", "OK", null);
        } catch(IOException e) {
            e.printStackTrace();
            showAlertDialog("Unable to delete archives!", "OK", null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
            return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        SaveState();
        super.onConfigurationChanged(newConfig);
        InitializeStateSave();
    }

    @Override
    public void onBackPressed()
    {
        if (isEditing) {
            isAdmin = true;
            isEditing = false;
            SetAdministratorLayout();
        }
        else if (isAdmin) {
            isAdmin = false;
            isEditing = false;
            SetUserLayout();
        }
        else {
            SaveState();
            System.exit(0);
        }
    }

    @Override
    public void onPause()
    {
        if (isAdmin) {
            SetUserLayout();
            SaveState();
            isAdmin = false;
        }
        else {
            SaveState();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        SaveState();
        super.onDestroy();
    }

    public class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            String DY = "";
            String MO = "";
            String YR = Integer.toString(year);
            if (day < 10) DY = "0";
            DY += Integer.toString(day);
            if (month < 9) MO = "0";
            MO += Integer.toString(month + 1);
            txtDate.setText(MO + "/" + DY + "/" + YR);
        }
    }

    public class AlertDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(MESSAGE);
            builder.setPositiveButton(POSITIVE_BUTTON, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    switch (actionRequested) {
                        case TRANSFER_FORMS:
                            PrepareFormTransfer();
                            PrepareToTransfer(TEMP_FILE);
                            SaveTempFile();
                            break;
                        case TRANSFER_ALL_ARCHIVES:
                            PrepareBulkTransfer();
                            PrepareToTransfer(BULK_FILE);
                            break;
                        case RECEIVE_NAMES:
                            RetrieveComputerFile(VOLUNTEERS_FILE);
                            break;
                        case RECEIVE_DESCRIPTIONS:
                            RetrieveComputerFile(DESCRIPTIONS_FILE);
                            break;
                        case RECEIVE_KEYWORDS:
                            RetrieveComputerFile(KEYWORDS_FILE);
                            break;
                        case CHANGE_PASSWORD:
                            SetChangePasswordLayout();
                            break;
                        case DELETE_ALL_ARCHIVES:
                            DeleteAllArchives();
                    }
                    actionRequested = -1;
                }
            });
            try {
                if (!(null == NEGATIVE_BUTTON)) {
                    builder.setNegativeButton(NEGATIVE_BUTTON, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            return builder.create();

        }

    }

}