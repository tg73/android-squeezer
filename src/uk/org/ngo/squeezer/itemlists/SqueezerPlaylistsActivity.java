/*
 * Copyright (c) 2011 Kurt Aaholst <kaaholst@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.org.ngo.squeezer.itemlists;

import java.util.List;

import uk.org.ngo.squeezer.R;
import uk.org.ngo.squeezer.framework.SqueezerBaseListActivity;
import uk.org.ngo.squeezer.framework.SqueezerItemView;
import uk.org.ngo.squeezer.itemlists.dialogs.SqueezerPlaylistsNewDialog;
import uk.org.ngo.squeezer.model.SqueezerPlaylist;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class SqueezerPlaylistsActivity extends SqueezerBaseListActivity<SqueezerPlaylist>{
    private int currentIndex = -1;
	private SqueezerPlaylist currentPlaylist;
    private String oldname;
    public SqueezerPlaylist getCurrentPlaylist() { return currentPlaylist; }

    /**
     * Set the playlist to be used as context
     * @param index
     * @param playlist
     */
    public void setCurrentPlaylist(int index, SqueezerPlaylist playlist) {
	    this.currentIndex = index;
	    this.currentPlaylist = playlist;
	    getIntent().putExtra("currentPlaylist", currentPlaylist);
	}

    /**
     * Rename the playlist previously set as context.
     * @param newname
     */
    public void playlistRename(String newname) {
        try {
            getService().playlistsRename(getCurrentPlaylist(), newname);
            if (currentIndex != -1) {
                oldname = currentPlaylist.getName();
                getCurrentPlaylist().setName(newname);
                getItemAdapter().notifyDataSetChanged();
            } else
                // We don't know which item to update, so we just reorder
                orderItems();
        } catch (RemoteException e) {
            Log.e(getTag(), "Error renaming playlist to '"+ newname + "': " + e);
        }
    }

	@Override
	public SqueezerItemView<SqueezerPlaylist> createItemView() {
		return new SqueezerPlaylistView(this);
	}

	@Override
	protected void registerCallback() throws RemoteException {
		getService().registerPlaylistsCallback(playlistsCallback);
		getService().registerPlaylistMaintenanceCallback(playlistMaintenanceCallback);
	}

	@Override
	protected void unregisterCallback() throws RemoteException {
		getService().unregisterPlaylistsCallback(playlistsCallback);
		getService().unregisterPlaylistMaintenanceCallback(playlistMaintenanceCallback);
	}

	@Override
	protected void orderPage(int start) throws RemoteException {
		getService().playlists(start);
	}

	@Override
	public void prepareActivity(android.os.Bundle extras) {
        if (extras != null) {
            currentPlaylist = extras.getParcelable("currentPlaylist");
        }
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.playlistsmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_playlists_new:
		    new SqueezerPlaylistsNewDialog().show(getSupportFragmentManager(), SqueezerPlaylistsNewDialog.class.getName());
		    return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	public static void show(Context context) {
        final Intent intent = new Intent(context, SqueezerPlaylistsActivity.class);
        context.startActivity(intent);
    }

    private final IServicePlaylistsCallback playlistsCallback = new IServicePlaylistsCallback.Stub() {
		public void onPlaylistsReceived(int count, int start, List<SqueezerPlaylist> items) throws RemoteException {
			onItemsReceived(count, start, items);
		}
    };

    private void showServiceMessage(final String msg) {
		getUIThreadHandler().post(new Runnable() {
			public void run() {
				getItemAdapter().notifyDataSetChanged();
				Toast.makeText(SqueezerPlaylistsActivity.this, msg, Toast.LENGTH_SHORT).show();
			}
		});
    }

    private final IServicePlaylistMaintenanceCallback playlistMaintenanceCallback = new IServicePlaylistMaintenanceCallback.Stub() {

		public void onRenameFailed(String msg) throws RemoteException {
		    if (currentIndex != -1)
		        currentPlaylist.setName(oldname);
			showServiceMessage(msg);
		}

		public void onCreateFailed(String msg) throws RemoteException {
			showServiceMessage(msg);
		}

    };

}
