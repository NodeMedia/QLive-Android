package xyz.tanwb.airship.imgsel;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import xyz.tanwb.airship.App;
import xyz.tanwb.airship.R;
import xyz.tanwb.airship.utils.ScreenUtils;
import xyz.tanwb.airship.utils.ToastUtils;
import xyz.tanwb.airship.view.BaseView;
import xyz.tanwb.airship.view.adapter.BaseListAdapter;
import xyz.tanwb.airship.view.adapter.BaseRecyclerDivider;
import xyz.tanwb.airship.view.adapter.BaseRecyclerMultiItemAdapter;
import xyz.tanwb.airship.view.adapter.ViewHolderHelper;
import xyz.tanwb.airship.view.adapter.listener.OnItemClickListener;
import xyz.tanwb.airship.view.contract.PhotoPresenter;

public interface ImgselContract {

    interface View extends BaseView {

        RecyclerView getRecyclerView();

        ImgSelConfig getImgSelConfig();

        void setBtnAlbumSelected(String content);
    }

    class Presenter extends PhotoPresenter<View> {

        private ImgSelConfig config;

        private RecyclerView commonRecycler;
        private ListPopupWindow folderPopupWindow;

        private ImageAdapter imageAdapter;
        private FolderAdapter folderAdapter;

        private int selFolder;

        private ArrayList<String> selImageList = new ArrayList<>();

        @Override
        public void onStart() {
            super.onStart();
            config = mView.getImgSelConfig();
            commonRecycler = mView.getRecyclerView();

            imageAdapter = new ImageAdapter(mContext, config.loader);
            imageAdapter.openLoadAnimation();
            imageAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(android.view.View v, int position) {
                    ImageInfo imageInfo = imageAdapter.getItem(position);
                    if (config.needCamera && position == 0) {
                        openSystemCamera();
                    } else {
                        if (imageInfo != null) {
                            if (config.maxNum > 1) {
                                if (imageInfo.isSelecte) {
                                    selImageList.remove(imageInfo.path);
                                    imageInfo.isSelecte = false;
                                } else {
                                    if (config.maxNum > getSelImageSize()) {
                                        selImageList.add(imageInfo.path);
                                        imageInfo.isSelecte = true;
                                    } else {
                                        ToastUtils.show(mContext, String.format(mContext.getString(R.string.imgsel_maxnum), config.maxNum));
                                        return;
                                    }
                                }
                                imageAdapter.notifyDataSetChanged();
                                mActivity.invalidateOptionsMenu();
                            } else {
                                if (config.needCrop) {
                                    photoFile = openPhotoCut(new File(imageInfo.path));
                                } else {
                                    // Log.e("imageInfo.path:" + imageInfo.path);
                                    selImageList.clear();
                                    selImageList.add(imageInfo.path);
                                    resultExit();
                                }
                            }
                        }
                    }
                }
            });

            commonRecycler.setLayoutManager(new GridLayoutManager(mContext, config.spanCount));
            commonRecycler.addItemDecoration(new BaseRecyclerDivider());
            commonRecycler.setAdapter(imageAdapter);

            folderAdapter = new FolderAdapter(mContext, config.loader);

            setPhotoCut(config.needCrop);
            setCutRect(config.aspectX, config.aspectY, config.outputX, config.outputY);
            questPermissions();
        }

        @Override
        public void onPermissionsSuccess(String[] permissions) {
            LoaderManager.LoaderCallbacks loaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {

                private final String[] imageProjection = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.Media._ID};

                @Override
                public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                    return new CursorLoader(mContext, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageProjection, null, null, imageProjection[2] + " DESC");
                }

                @Override
                public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                    if (data != null) {
                        imageAdapter.clearDatas();
                        int count = data.getCount();
                        if (count > 0) {

                            List<FolderInfo> folderInfoList = new ArrayList<>();

                            FolderInfo allFolderInfo = new FolderInfo("所有图片");
                            folderInfoList.add(allFolderInfo);

                            data.moveToFirst();
                            do {
                                String path = data.getString(data.getColumnIndexOrThrow(imageProjection[0]));
                                if (path.endsWith("gif")) {
                                    break;
                                }
                                String name = data.getString(data.getColumnIndexOrThrow(imageProjection[1]));
                                long dateTime = data.getLong(data.getColumnIndexOrThrow(imageProjection[2]));

                                ImageInfo imageInfo = new ImageInfo(path, name, dateTime);

                                File imageFile = new File(path);
                                File folderFile = imageFile.getParentFile();
                                FolderInfo folderInfo = new FolderInfo(folderFile.getAbsolutePath(), folderFile.getName(), path);

                                if (!folderInfoList.contains(folderInfo)) {
                                    folderInfoList.add(folderInfo);
                                } else {
                                    folderInfo = folderInfoList.get(folderInfoList.indexOf(folderInfo));
                                }

                                folderInfo.imageInfos.add(imageInfo);
                                allFolderInfo.imageInfos.add(imageInfo);

                            } while (data.moveToNext());

                            selFolder = 0;

                            folderAdapter.setDatas(folderInfoList);

                            imageAdapter.setDatas(allFolderInfo.imageInfos);
                        }
                        if (config.needCamera) {
                            imageAdapter.addData(0, new ImageInfo());
                        }
                    }
                }

                @Override
                public void onLoaderReset(Loader<Cursor> loader) {
                }
            };
            mActivity.getLoaderManager().initLoader(0, null, loaderCallbacks);
        }

        @Override
        public void onPermissionsFailure(String strMsg) {
            super.onPermissionsFailure(strMsg);
            new AlertDialog.Builder(mContext).setTitle("权限提示").setMessage("该操作需要您允许使用摄像头和存储读写权限，去打开吗？").setCancelable(false).
                    setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            App.openPermissionSetting(mActivity);
                            mView.exit();
                        }
                    }).
                    setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mView.exit();
                        }
                    }).show();
        }

        public void onClick(android.view.View anchor) {
            if (folderPopupWindow == null) {
                createPopupFolderList(anchor);
            }
            if (folderPopupWindow.isShowing()) {
                folderPopupWindow.dismiss();
            } else {
                folderPopupWindow.show();
            }
        }

        private void createPopupFolderList(android.view.View anchor) {

            int width = ScreenUtils.getScreenWidth() * 2 / 3;
            int height = ScreenUtils.getScreenHeight() * 2 / 3;

            folderPopupWindow = new ListPopupWindow(mContext);
            folderPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            folderPopupWindow.setAdapter(folderAdapter);
            folderPopupWindow.setContentWidth(width);
            folderPopupWindow.setWidth(width);
            folderPopupWindow.setHeight(height);
            folderPopupWindow.setAnchorView(anchor);
            folderPopupWindow.setModal(true);
            folderPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, android.view.View view, int position, long id) {
                    folderPopupWindow.dismiss();
                    if (selFolder != position) {
                        folderAdapter.getItem(selFolder).isSelecte = false;
                        selFolder = position;
                        FolderInfo folderInfo = folderAdapter.getItem(position);
                        mView.setBtnAlbumSelected(folderInfo.name);

                        folderInfo.isSelecte = true;
                        imageAdapter.setDatas(folderInfo.imageInfos);
                        if (config.needCamera) {
                            imageAdapter.addData(0, new ImageInfo());
                        }
                    }
                }
            });
        }


        @Override
        public void onPhotoSuccess(int photoType, File photoFile) {
            selImageList.clear();
            selImageList.add(photoFile.getAbsolutePath());
            resultExit();
        }

        public void resultExit() {
            if (getSelImageSize() > 0) {
                Intent intent = new Intent();
                intent.putExtra("p0", JSON.toJSONString(selImageList));
                mActivity.setResult(Activity.RESULT_OK, intent);
                selImageList.clear();
            }
            mView.exit();
        }

        public int getSelImageSize() {
            return selImageList == null ? 0 : selImageList.size();
        }

    }

    class ImageAdapter extends BaseRecyclerMultiItemAdapter<ImageInfo> {

        private ImageLoader loader;

        ImageAdapter(Context context, ImageLoader loader) {
            super(context, null);
            this.loader = loader;
            addItemType(0, R.layout.item_imgsel_camera);
            addItemType(1, R.layout.item_imgsel_image);
        }

        @Override
        protected void setMultiData(ViewHolderHelper viewHolderHelper, int position, ImageInfo model) {
            if (model.getItemType() == 1) {
                ImageView ivImage = viewHolderHelper.getView(R.id.imgsel_image);
                loader.displayImage(mContext, model.path, ivImage);
                viewHolderHelper.setImageResource(R.id.imgsel_cheaked, model.isSelecte ? R.drawable.imgsel_checked : R.drawable.imgsel_uncheck);
            }
        }
    }

    class FolderAdapter extends BaseListAdapter<FolderInfo> {

        private ImageLoader loader;

        FolderAdapter(Context context, ImageLoader loader) {
            super(context, R.layout.item_imgsel_folder);
            this.loader = loader;
        }

        @Override
        protected void setItemData(ViewHolderHelper viewHolderHelper, int position, FolderInfo folderInfo) {
            ImageView ivFolder = viewHolderHelper.getView(R.id.imgsel_folder_image);
            if (position > 0) {
                loader.displayImage(mContext, folderInfo.cover, ivFolder);
            } else {
                ivFolder.setImageResource(R.drawable.icon_default_image);
            }
            viewHolderHelper.setText(R.id.imgsel_folder_name, folderInfo.name);
            viewHolderHelper.setText(R.id.imgsel_folder_num, "共" + folderInfo.imageInfos.size() + "张");
            viewHolderHelper.setVisibility(R.id.imgsel_folder_indicator, folderInfo.isSelecte ? android.view.View.VISIBLE : android.view.View.GONE);
        }
    }

}
