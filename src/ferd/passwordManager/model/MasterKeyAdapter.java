package ferd.passwordManager.model;

import com.google.common.primitives.UnsignedInteger;
import com.lyndir.masterpassword.MPSiteType;
import com.lyndir.masterpassword.MPSiteVariant;
import com.lyndir.masterpassword.MasterKey;

public class MasterKeyAdapter {

    private final MasterKey mk;

    public MasterKeyAdapter(final String fullName, final String masterPassword) {
        this.mk = MasterKey.create(fullName, masterPassword.toCharArray());
    }

    public String encode(final String siteName, final MPSiteType siteType, final int siteCounter) {
        return this.mk.encode(siteName, siteType, UnsignedInteger.valueOf(siteCounter), MPSiteVariant.Password, null);
    }

}
