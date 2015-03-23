package org.sufficientlysecure.keychain.service.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;


public class RequiredInputParcel implements Parcelable {

    public enum RequiredInputType {
        PASSPHRASE, NFC_SIGN, NFC_DECRYPT
    }

    public Date mSignatureTime;

    public final RequiredInputType mType;

    public final byte[][] mInputHashes;
    public final int[] mSignAlgos;

    private Long mMasterKeyId;
    private Long mSubKeyId;

    private RequiredInputParcel(RequiredInputType type, byte[][] inputHashes,
            int[] signAlgos, Date signatureTime, Long masterKeyId, Long subKeyId) {
        mType = type;
        mInputHashes = inputHashes;
        mSignAlgos = signAlgos;
        mSignatureTime = signatureTime;
        mMasterKeyId = masterKeyId;
        mSubKeyId = subKeyId;
    }

    public RequiredInputParcel(Parcel source) {
        mType = RequiredInputType.values()[source.readInt()];

        if (source.readInt() != 0) {
            int count = source.readInt();
            mInputHashes = new byte[count][];
            mSignAlgos = new int[count];
            for (int i = 0; i < count; i++) {
                mInputHashes[i] = source.createByteArray();
                mSignAlgos[i] = source.readInt();
            }
        } else {
            mInputHashes = null;
            mSignAlgos = null;
        }

        mSignatureTime = source.readInt() != 0 ? new Date(source.readLong()) : null;
        mMasterKeyId = source.readInt() != 0 ? source.readLong() : null;
        mSubKeyId = source.readInt() != 0 ? source.readLong() : null;

    }

    public long getMasterKeyId() {
        return mMasterKeyId;
    }

    public long getSubKeyId() {
        return mSubKeyId;
    }

    public static RequiredInputParcel createNfcSignOperation(
            byte[] inputHash, int signAlgo, Date signatureTime) {
        return new RequiredInputParcel(RequiredInputType.NFC_SIGN,
                new byte[][] { inputHash }, new int[] { signAlgo },
                signatureTime, null, null);
    }

    public static RequiredInputParcel createNfcDecryptOperation(byte[] inputHash) {
        return new RequiredInputParcel(RequiredInputType.NFC_DECRYPT,
                new byte[][] { inputHash }, null, null, null, null);
    }

    public static RequiredInputParcel createRequiredPassphrase(
            long masterKeyId, long subKeyId, Date signatureTime) {
        return new RequiredInputParcel(RequiredInputType.PASSPHRASE,
                null, null, signatureTime, masterKeyId, subKeyId);
    }

    public static RequiredInputParcel createRequiredPassphrase(
            RequiredInputParcel req) {
        return new RequiredInputParcel(RequiredInputType.PASSPHRASE,
                null, null, req.mSignatureTime, req.mMasterKeyId, req.mSubKeyId);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mType.ordinal());
        if (mInputHashes != null) {
            dest.writeInt(1);
            dest.writeInt(mInputHashes.length);
            for (int i = 0; i < mInputHashes.length; i++) {
                dest.writeByteArray(mInputHashes[i]);
                dest.writeInt(mSignAlgos[i]);
            }
        } else {
            dest.writeInt(0);
        }
        if (mSignatureTime != null) {
            dest.writeInt(1);
            dest.writeLong(mSignatureTime.getTime());
        } else {
            dest.writeInt(0);
        }
        if (mMasterKeyId != null) {
            dest.writeInt(1);
            dest.writeLong(mMasterKeyId);
        } else {
            dest.writeInt(0);
        }
        if (mSubKeyId != null) {
            dest.writeInt(1);
            dest.writeLong(mSubKeyId);
        } else {
            dest.writeInt(0);
        }

    }

    public static final Creator<RequiredInputParcel> CREATOR = new Creator<RequiredInputParcel>() {
        public RequiredInputParcel createFromParcel(final Parcel source) {
            return new RequiredInputParcel(source);
        }

        public RequiredInputParcel[] newArray(final int size) {
            return new RequiredInputParcel[size];
        }
    };

    public static class NfcSignOperationsBuilder {
        Date mSignatureTime;
        ArrayList<Integer> mSignAlgos = new ArrayList<>();
        ArrayList<byte[]> mInputHashes = new ArrayList<>();
        long mMasterKeyId;
        long mSubKeyId;

        public NfcSignOperationsBuilder(Date signatureTime, long masterKeyId, long subKeyId) {
            mSignatureTime = signatureTime;
            mMasterKeyId = masterKeyId;
            mSubKeyId = subKeyId;
        }

        public RequiredInputParcel build() {
            byte[][] inputHashes = new byte[mInputHashes.size()][];
            mInputHashes.toArray(inputHashes);
            int[] signAlgos = new int[mSignAlgos.size()];
            for (int i = 0; i < mSignAlgos.size(); i++) {
                signAlgos[i] = mSignAlgos.get(i);
            }

            return new RequiredInputParcel(RequiredInputType.NFC_SIGN,
                    inputHashes, signAlgos, mSignatureTime, mMasterKeyId, mSubKeyId);
        }

        public void addHash(byte[] hash, int algo) {
            mInputHashes.add(hash);
            mSignAlgos.add(algo);
        }

        public void addAll(RequiredInputParcel input) {
            if (!mSignatureTime.equals(input.mSignatureTime)) {
                throw new AssertionError("input times must match, this is a programming error!");
            }
            if (input.mType != RequiredInputType.NFC_SIGN) {
                throw new AssertionError("operation types must match, this is a progrmming error!");
            }

            Collections.addAll(mInputHashes, input.mInputHashes);
            for (int signAlgo : input.mSignAlgos) {
                mSignAlgos.add(signAlgo);
            }
        }

        public boolean isEmpty() {
            return mInputHashes.isEmpty();
        }

    }

}
