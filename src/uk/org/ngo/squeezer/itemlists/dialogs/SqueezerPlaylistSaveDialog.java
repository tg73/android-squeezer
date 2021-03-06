package uk.org.ngo.squeezer.itemlists.dialogs;

import uk.org.ngo.squeezer.R;
import uk.org.ngo.squeezer.framework.SqueezerBaseActivity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.InputType;
import android.util.Log;

public class SqueezerPlaylistSaveDialog extends SqueezerBaseEditTextDialog {
    private SqueezerBaseActivity activity;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        activity = (SqueezerBaseActivity) getActivity();
        dialog.setTitle(R.string.save_playlist_title);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editText.setHint(R.string.save_playlist_hint);

        return dialog;
    };

    @Override
    protected boolean commit(String name) {
        try {
            activity.getService().playlistSave(name);
        } catch (RemoteException e) {
            Log.e(getTag(), "Error saving playlist as '"+ name + "': " + e);
        }
        return true;
    }

}
