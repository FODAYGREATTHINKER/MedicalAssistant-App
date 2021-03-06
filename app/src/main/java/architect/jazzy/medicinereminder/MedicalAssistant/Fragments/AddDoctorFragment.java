package architect.jazzy.medicinereminder.MedicalAssistant.Fragments;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;

import architect.jazzy.medicinereminder.HelperClasses.Constants;
import architect.jazzy.medicinereminder.MedicalAssistant.Handlers.DataHandler;
import architect.jazzy.medicinereminder.MedicalAssistant.Handlers.RealPathUtil;
import architect.jazzy.medicinereminder.MedicalAssistant.Models.Doctor;
import architect.jazzy.medicinereminder.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddDoctorFragment extends Fragment {

  public static final int CONTACT_PICK_REQUEST_CODE = 120;
  public static final int READ_CONTACTS_CODE = 15934;
  private static final String TAG = "AddDoctorFragment";
  private static final int COVER_PIC_REQUEST_CODE = 121;
  View v;
  ImageView imageView;
  Doctor doctor;
  Button save;
  EditText docName, docPhone1, docPhone2, docAddress, docHospital, docNotes;
  OnFragmentInteractionListener onFragmentInteractionListener;
  private Uri uriContact;
  private String contactID;

  public AddDoctorFragment() {
    // Required empty public constructor
  }

  public static AddDoctorFragment newInstance(Doctor doctor) {
    AddDoctorFragment fragment = new AddDoctorFragment();
    Bundle args = new Bundle();
    args.putParcelable(Constants.BUNDLE_DOCTOR, doctor);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    v = inflater.inflate(R.layout.fragment_add_doctor, container, false);


    imageView = (ImageView) v.findViewById(R.id.docPic);
    docAddress = (EditText) v.findViewById(R.id.docAddress);
    docHospital = (EditText) v.findViewById(R.id.docHospital);
    docName = (EditText) v.findViewById(R.id.docName);
    docPhone1 = (EditText) v.findViewById(R.id.docPhone1);
    docPhone2 = (EditText) v.findViewById(R.id.docPhone2);
    docNotes = (EditText) v.findViewById(R.id.docNotes);
    save = (Button) v.findViewById(R.id.saveButton);
    save.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        saveDoctor();
      }
    });

    docName.setSelected(true);

    setEditTextIcons(Constants.getThemeColor(getActivity()));

    try {
      doctor = getArguments().getParcelable(Constants.BUNDLE_DOCTOR);
    } catch (NullPointerException e) {
      doctor = new Doctor();
    }
    updateForm();
    setHasOptionsMenu(true);

    imageView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent i;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
          i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        } else {
          i = new Intent(Intent.ACTION_PICK);
        }
        i.setType("image/*");
        startActivityForResult(Intent.createChooser(i, "Select Doctor's Photo"), COVER_PIC_REQUEST_CODE);
      }
    });

    imageView.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        imageView.setImageResource(R.drawable.userlogin);
        doctor.setPhoto("");
        return false;
      }
    });
    return v;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    try {
      ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    } catch (NullPointerException e) {
      e.printStackTrace();
    }
    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Add Doctor");
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, final Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    Log.e(TAG, "onActivityResult: " + requestCode + " " + resultCode);

    if (resultCode == Activity.RESULT_OK) {
      if (requestCode == CONTACT_PICK_REQUEST_CODE) {
        uriContact = data.getData();
        retrieveContactDetails();
      }
      if (requestCode == COVER_PIC_REQUEST_CODE) {
        Log.e(TAG, "Doctor image path: " + data.getData().toString());
        final String imagePath = RealPathUtil.getPathFromURI(getActivity(), data.getData());
        try {
          Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
              Bitmap bitmap = null;
              Looper.getMainLooper();
              try {
                bitmap = Picasso.with(getActivity())
                    .load(imagePath)
                    .get();
              } catch (IOException e) {
                e.printStackTrace();
              }
              doctor.setPhotoPath(imagePath);
              try {
                if (bitmap == null) {
                  BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
                  bitmap = drawable.getBitmap();
                }
                Palette.Builder builder = Palette.from(bitmap);
                Palette palette = builder.generate();

                try {
                  final int color = Color.HSVToColor(palette.getSwatches().get(0).getHsl());

                  final Bitmap finalBitmap = bitmap;
                  imageView.post(new Runnable() {
                    @Override
                    public void run() {
                      imageView.getLayoutParams().height = finalBitmap.getHeight();
                      imageView.requestLayout();
                      imageView.setImageBitmap(Constants.getScaledBitmap(imagePath, imageView.getMeasuredWidth(), imageView.getMeasuredHeight()));
                      setEditTextIcons(color);
                    }
                  });
                } catch (NullPointerException e) {
                  e.printStackTrace();
                }
              } catch (Exception e) {
                e.printStackTrace();
              }

            }
          });
          thread.start();
        } catch (Exception e) {
          e.printStackTrace();
        }


      }
    }
  }

  void retrieveContactDetails() {
    retrieveContactNumber();
  }

  void updateForm() {
    if (doctor == null) {
      doctor = new Doctor();
    }
    docName.setText(doctor.getName());
    docAddress.setText(doctor.getAddress());
    docPhone1.setText(doctor.getPhone_1());
    docPhone2.setText(doctor.getPhone_2());
    docNotes.setText(doctor.getNotes());
    try {
      imageView.setImageURI(doctor.getPhotoUri());
      BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
      Bitmap bitmap = drawable.getBitmap();
      Palette.Builder builder = Palette.from(bitmap);
      Palette palette = builder.generate();

      try {
        int color = Color.HSVToColor(palette.getSwatches().get(0).getHsl());
        setEditTextIcons(color);
      } catch (NullPointerException e) {
        e.printStackTrace();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  void setEditTextIcons(int color) {
    try {
      Constants.scaleEditTextImage(getActivity(), docPhone1, R.drawable.ic_call_black_24dp, color);
      Constants.scaleEditTextImage(getActivity(), docPhone2, R.drawable.ic_call_black_24dp, color);
      Constants.scaleEditTextImage(getActivity(), docAddress, R.drawable.ic_location_city_black_24dp, color, false);
      Constants.scaleEditTextImage(getActivity(), docHospital, R.drawable.ic_business_black_24dp, color, false);
      Constants.scaleEditTextImage(getActivity(), docNotes, R.drawable.ic_action_edit, color);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  void saveDoctor() {
    if (docName.getText().toString().length() < 3) {
      docName.setError("Name cannot be less than 3 characters");
      return;
    }
    if (docPhone1.getText().toString().isEmpty() && docPhone2.getText().toString().isEmpty()) {
      docPhone1.setError("Phone number cannot be empty");
      return;
    }

    doctor.setName(docName.getText().toString());
    doctor.setPhone_1(docPhone1.getText().toString());
    doctor.setPhone_2(docPhone2.getText().toString());
    doctor.setAddress(docAddress.getText().toString());
    doctor.setNotes(docNotes.getText().toString());
    doctor.setHospital(docHospital.getText().toString());

    Log.e(TAG, "Saving doctor: " + doctor.toJSON());


    DataHandler dataHandler = new DataHandler(getActivity());
    dataHandler.insertDoctor(doctor);
    dataHandler.close();
    Toast.makeText(getActivity(), "Doctor details saved", Toast.LENGTH_SHORT).show();
    onFragmentInteractionListener.showDoctors();
  }

  void retrieveContactNumber() {
    ContentResolver contentResolver = getActivity().getContentResolver();
    Cursor cursor = contentResolver.query(uriContact, null, null, null, null);
    if (cursor == null) {
      Toast.makeText(getActivity(), "Unable to fetch contact details. Try again.", Toast.LENGTH_LONG).show();
      return;
    }
    if (cursor.moveToFirst()) {
      doctor.setName(retrieveContactName());
      doctor.setPhotoUri(retrieveContactUri(cursor));
      Log.e(TAG, "Retreive contact number: " + doctor.getPhotoUri() + "\nOriginal: " + retrieveContactUri(cursor));
      doctor.setId(retrieveContactId(cursor));

      if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
        Cursor phoneCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
            new String[]{doctor.getId()},
            null);
        if (phoneCursor == null) {
          Toast.makeText(getActivity(), "Error fetching contact details. Try Again", Toast.LENGTH_LONG).show();
          return;
        }
        phoneCursor.moveToFirst();
        int i = 1;
        do {
          String phone = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
          if (i == 1) {
            doctor.setPhone_1(phone);
          } else if (i == 2) {
            doctor.setPhone_2(phone);
          } else {
            break;
          }
          i++;
        } while (phoneCursor.moveToNext());
        phoneCursor.close();
      }
      cursor.close();
    }
    updateForm();
  }

  Uri retrieveContactUri(Cursor cursor) {
    try {
      return Uri.parse(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI)));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  String retrieveContactId(Cursor cursor) {
    return contactID = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
  }

  String retrieveContactName() {
    String contactName = "";
    Cursor cursor = getActivity().getContentResolver().query(uriContact, null, null, null, null);
    if (cursor == null) {
      Toast.makeText(getActivity(), "Unable to read contact name", Toast.LENGTH_LONG).show();
      return "";
    }
    if (cursor.moveToFirst()) {
      contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
    }
    cursor.close();
//        Log.e(TAG,"Contact Name: "+contactName);
    return contactName;
  }

  public void selectDoctorFromContacts() {
    final Activity context = getActivity();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.READ_CONTACTS)) {
          AlertDialog.Builder builder = new AlertDialog.Builder(context, 0);
          builder.setTitle("Permission required")
              .setMessage("Permission is required for reading contacts.")
              .setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                @Override
                @TargetApi(23)
                public void onClick(DialogInterface dialogInterface, int i) {
                  requestPermissions(
                      new String[]{Manifest.permission.READ_CONTACTS},
                      READ_CONTACTS_CODE);
                }
              })
              .show();

        } else {
          requestPermissions(
              new String[]{Manifest.permission.READ_CONTACTS},
              READ_CONTACTS_CODE);
        }
      } else {
        Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(i, CONTACT_PICK_REQUEST_CODE);
      }
    } else {
      Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
      startActivityForResult(i, CONTACT_PICK_REQUEST_CODE);
    }
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_contact_select, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.addDoctor) {
      selectDoctorFromContacts();
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  @TargetApi(21)
  public void onAttach(Context context) {
    super.onAttach(context);
    onFragmentInteractionListener = (OnFragmentInteractionListener) context;
  }

  @Override
  @TargetApi(14)
  @SuppressWarnings("deprecation")
  public void onAttach(Activity context) {
    super.onAttach(context);
    onFragmentInteractionListener = (OnFragmentInteractionListener) context;
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    Log.e(TAG, "onRequestPermissionResult: " + requestCode);
    switch (requestCode) {
      case AddDoctorFragment.READ_CONTACTS_CODE:
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
          startActivityForResult(i, CONTACT_PICK_REQUEST_CODE);
        } else {
          Toast.makeText(getActivity(), "Permission required to read contacts", Toast.LENGTH_LONG).show();
        }
        break;
      default:
        break;
    }
  }

  public interface OnFragmentInteractionListener {
    void showDoctors();
  }
}
