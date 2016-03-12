package ferd.passwordManager.model;

import com.lyndir.masterpassword.MPSiteType;

import javafx.util.StringConverter;

public final class MPSiteTypeConverter extends StringConverter<MPSiteType> {
    @Override
    public MPSiteType fromString(final String string) {
        return MPSiteType.forName("Generated" + string);
    }

    @Override
    public String toString(final MPSiteType object) {
        return object.getShortName();
    }
}
