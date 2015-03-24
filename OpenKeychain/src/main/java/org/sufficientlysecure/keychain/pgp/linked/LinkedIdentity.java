package org.sufficientlysecure.keychain.pgp.linked;

import org.spongycastle.bcpg.UserAttributeSubpacket;
import org.spongycastle.util.Strings;
import org.sufficientlysecure.keychain.Constants;
import org.sufficientlysecure.keychain.pgp.WrappedUserAttribute;
import org.sufficientlysecure.keychain.util.Log;

import java.io.IOException;
import java.net.URI;

import android.content.Context;
import android.support.annotation.DrawableRes;


public class LinkedIdentity extends RawLinkedIdentity {

    public final LinkedResource mResource;

    protected LinkedIdentity(URI uri, LinkedResource resource) {
        super(uri);
        if (resource == null) {
            throw new AssertionError("resource must not be null in a LinkedIdentity!");
        }
        mResource = resource;
    }

    public static RawLinkedIdentity fromAttributeData(byte[] data) throws IOException {
        WrappedUserAttribute att = WrappedUserAttribute.fromData(data);

        byte[][] subpackets = att.getSubpackets();
        if (subpackets.length >= 1) {
            return fromSubpacketData(subpackets[0]);
        }

        throw new IOException("no subpacket data");
    }

    /** This method parses a linked id from a UserAttributeSubpacket, or returns null if the
     * subpacket can not be parsed as a valid linked id.
     */
    static RawLinkedIdentity fromAttributeSubpacket(UserAttributeSubpacket subpacket) {
        if (subpacket.getType() != 101) {
            return null;
        }

        byte[] data = subpacket.getData();

        return fromSubpacketData(data);
    }

    static RawLinkedIdentity fromSubpacketData(byte[] data) {

        try {
            String uriStr = Strings.fromUTF8ByteArray(data);
            URI uri = URI.create(uriStr);

            LinkedResource res = LinkedResource.fromUri(uri);
            if (res == null) {
                return new RawLinkedIdentity(uri);
            }

            return new LinkedIdentity(uri, res);

        } catch (IllegalArgumentException e) {
            Log.e(Constants.TAG, "error parsing uri in (suspected) linked id packet");
            return null;
        }
    }

    public static RawLinkedIdentity fromResource (LinkedCookieResource res) {
        return new RawLinkedIdentity(res.toUri());
    }


    public @DrawableRes int getDisplayIcon() {
        return mResource.getDisplayIcon();
    }

    public String getDisplayTitle(Context context) {
        return mResource.getDisplayTitle(context);
    }

    public String getDisplayComment(Context context) {
        return mResource.getDisplayComment(context);
    }

}