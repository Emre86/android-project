package homesweethome.emre.mytracker;

import android.provider.BaseColumns;

/**
 * Created by emre on 19/08/16.
 */
public final class FeedReaderContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public FeedReaderContract() {}

    /* Inner class that defines the table contents */
    public static abstract class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "TABLE_CONTACT";
        public static final String COLUMN_NAME_NAME = "Name_Name";
        public static final String COLUMN_NAME_TEL = "Name_tel";
    }
}
