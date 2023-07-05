package com.rohitthebest.manageyourrenters.others

import com.rohitthebest.manageyourrenters.BuildConfig

object Constants {

    const val UNSPLASH_BASE_URL = "https://api.unsplash.com/"

    const val NO_INTERNET_MESSAGE = "Please check your Internet connection!!!"
    const val EDIT_TEXT_EMPTY_MESSAGE = "*This is a mandatory field..."

    // ----- databases name RELATED---------
    const val RENTER_AND_PAYMENT_DATABASE_NAME = "renter_and_payment_database.db"
    const val BORROWER_DATABASE_NAME = "borrowerdatabase.db"
    const val BORROWER_PAYMENT_DATABASE_NAME = "borrowerpaymentdatabase.db"
    const val PARTIAL_PAYMENT_DATABASE_NAME = "partialpaymentdatabase.db"
    const val EMI_DATABASE_NAME = "emi_database.db"
    const val EXPENSE_DATABASE_NAME = "expense_database.db"
    const val MONTHLY_PAYMENT_DATABASE_NAME = "monthly_payment_db"
    const val PAYMENT_METHOD_DATABASE_NAME = "payment_method_db"
    const val BUDGET_AND_INCOME_DATABASE = "budget_income_database.db"
    //----------------------------

    //------------
    const val NOTIFICATION_CHANNEL_ID = "NotificationChannelID"
    //------------

    //-------- Firestore----------
    const val COLLECTION_KEY = "Colection_key_dskjsadaaddhadkjhbskjbvjhb"
    const val DOCUMENT_KEY = "Document_key_dskjshfjksadadbskjbvjhb"
    const val UPDATE_DOCUMENT_MAP_KEY = "UPDATE_DOCUMENT_MAP_KEYadsadsaDF_dskjshfdfsdadkjhbskjbvjhb"
    const val UPLOAD_DATA_KEY = "UPLOAD_TAG_sdhaasdfhkjvhkjvhjkvhbjkvb"
    const val RANDOM_ID_KEY = "RANDOM_ID_KEY_sdjhdsdvjkbvbavbhbvhjbhjbvdb"
    const val KEY_LIST_KEY = "KEY_LIST_KEY_fdhvjkhjkvkjvkbvk"
    const val DELETE_FILE_FROM_FIREBASE_KEY = "DELETE_FILE_FROM_FIREBASE_KEY_sjvvbibisbvbaib"
    //-----------------------------

    //--------- Shared Preference-----------
    const val IS_SYNCED_SHARED_PREF_NAME = "IS_SYNCED_SHARED_PREF_NAME_fdkdnf"
    const val IS_SYNCED_SHARED_PREF_KEY = "IS_SYNCED_SHARED_PREF_KEY_fkjdvkjdbdnf"
    const val CUSTOM_DATE_RANGE_FOR_GRAPH_FRAGMENT_SHARED_PREF_NAME = "djbbbwebhbBKJBJBSHBHBJbbsjb"
    const val CUSTOM_DATE_RANGE_FOR_GRAPH_FRAGMENT_SHARED_PREF_KEY = "sbjsbbhjsbhbyguqyJBABBHJBkbnd"
    // --------------------------------------

    const val SUPPORTING_DOCUMENT_HELPER_MODEL_KEY = "dsbjsbjhbuyavvu"
    const val RENTER_PAYMENT_CONFIRMATION_BILL_KEY = "BNZBNJBHVDLK&hjbxjhbskxknxB"

    //--- Menu BottomSheet related---------
    const val SHOW_EDIT_MENU = "SHOW_EDIT_MENU_sjcjwncjkn"
    const val SHOW_DOCUMENTS_MENU = "SHOW_DOCUMENTS_MENU_sjcjwncacacn"
    const val SHOW_DELETE_MENU = "DELETE_MENUbkjbskjc"
    const val SHOW_SYNC_MENU = "SYNC_MENUvxnnlznlbskjc"
    const val SHOW_MOVE_MENU = "MOVE_SAdhbhfbABJnbhsb"
    const val SHOW_COPY_MENU = "COPY_SANJABABJnbhhsb"
    const val COPY_MENU_TEXT = "COPY-TEXT_SwwAdsdrBAbuh=hsb"
    //---------------------------

    const val NETWORK_PAGE_SIZE_UNSPLASH = 30
    const val SHORTCUT_FRAGMENT_NAME_KEY = "sdcjnsknkanckbajcbabacjk"

    const val ONE_DAY_MILLISECONDS = 86400000L

    const val FILE_PROVIDER_AUTHORITY = "com.rohitthebest.manageyourrenters.provider"

    // -------- Shortcuts ---------
    const val SHORTCUT_EXPENSE = "com.rohitthebest.manageyourrenters.expense"
    const val SHORTCUT_BORROWERS = "com.rohitthebest.manageyourrenters.borrowers"
    const val SHORTCUT_HOUSE_RENTERS = "com.rohitthebest.manageyourrenters.house_renters"
    const val SHORTCUT_MONTHLY_PAYMENTS = "com.rohitthebest.manageyourrenters.monthly_payments"
    const val SHORTCUT_EMI = "com.rohitthebest.manageyourrenters.emis"
    // ----------------------------

    //--- app update related------
    const val APP_VERSION = BuildConfig.VERSION_NAME
    const val APP_UPDATE_FIRESTORE_DOCUMENT_KEY = "latest_app_version"
    //------------------------------

    //---- Default payment method keys-------
    const val PAYMENT_METHOD_CASH_KEY = "payment_method_cash_1316797_rrrrr"
    const val PAYMENT_METHOD_DEBIT_CARD_KEY = "payment_method_debit_card_5765763_rrrrr"
    const val PAYMENT_METHOD_CREDIT_CARD_KEY = "payment_method_credit_card_974673_rrrrr"
    const val PAYMENT_METHOD_OTHER_KEY = "payment_method_other_6546332_rrrrr"

    const val CASH_PAYMENT_METHOD = "💵 CASH"
    const val OTHER_PAYMENT_METHOD = "💰 OTHER"
    const val DEBIT_CARD_PAYMENT_METHOD = "💳 DEBIT CARD"
    const val CREDIT_CARD_PAYMENT_METHOD = "💳 CREDIT CARD"
    // --------------------------------------

    const val ADD_PAYMENT_METHOD_KEY =
        "add_payment_method_key" // will be used only for recycler view

    // PaymentMethod BottomSheet menu
    const val IS_FOR_EDIT = "is_for_edit_key"
    const val PAYMENT_METHOD_KEY_FOR_EDIT = "PAYMENT_METHOD_KEY_FOR_EDIT_KEY"

    const val EXPENSE_FILTER_KEY = "EXPENSE_FILTER_KEY_sdbjhsbjs"
    const val BUDGET = "passing_budget_object_using_bundle_key"

    const val SERVICE_STOP_TIME_IN_SECONDS: Long = 45
    const val GENERIC_KEY_FOR_ACTIVITY_OR_FRAGMENT_COMMUNICATION = "GENERIC_KEY_skdjhshjsvhjs"
    const val GENERIC_KEY_FOR_ACTIVITY_OR_FRAGMENT_COMMUNICATION2 =
        "GENERIC_KEY_2**cnxjhshjxfnvkjss"

    const val INCOME_MONTH_KEY = "hjsjhsvhjvsvyugsgfgwv"
    const val INCOME_YEAR_KEY = "sdghjsbhjsvhshghgschgc"

    //----------------------- Month and Year Picker Dialog ---------------------------
    const val MONTH_YEAR_PICKER_MONTH_KEY = "sbhsbhjbjhdvgsfycsgfcgfwc"
    const val MONTH_YEAR_PICKER_YEAR_KEY = "hsbjhsvhgvshgsgcfscgfcsg"
    const val MONTH_YEAR_PICKER_MIN_YEAR_KEY = "sdhbsjhhjsvgsvghsvhgshhgs"
    const val MONTH_YEAR_PICKER_MAX_YEAR_KEY = "jbhjsvgsygsfrtsfgctradxaa"
    //--------------------------------------------------------------------------------

    const val COPY_BUDGET_MONTH_AND_YEAR_KEY = "rdnjshbhsvygacfcfacfca"
}