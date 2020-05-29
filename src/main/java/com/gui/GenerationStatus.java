package com.gui;

// These values are used in the GUI to identify what kind of message dialog should
// be displayed when an exception is caught.
enum GenerationStatus {
    SUCCESS,
    ZIP_FORMAT_ERROR,
    COULD_NOT_READ_FILE_ERROR,
    COULD_NOT_SAVE_FILE_ERROR
}
