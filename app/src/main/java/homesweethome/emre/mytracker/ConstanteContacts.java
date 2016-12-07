package homesweethome.emre.mytracker;

import android.net.Uri;
import android.provider.ContactsContract;

/**
 * Created by emre on 16/08/16.
 */
public interface ConstanteContacts {
    Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
    String _ID = ContactsContract.Contacts._ID;
    String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
    String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;
    Uri Phone_CONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
    String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
    String Phone_NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
}
