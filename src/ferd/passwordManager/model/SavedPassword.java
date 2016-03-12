package ferd.passwordManager.model;

import java.io.Serializable;

import com.lyndir.masterpassword.MPSiteType;

public class SavedPassword implements Serializable {
    private static final long serialVersionUID = 2248358279341705264L;
    private String notes;
    private String website;
    private int counter;
    private MPSiteType mpSiteType;
    private int algoVersion;

    public SavedPassword(final String website, final int counter) {
        this(website, counter, null);
    }

    public SavedPassword(final String website, final int counter, final String notes) {
        this.notes = notes;
        this.website = website;
        this.counter = counter;
        this.mpSiteType = MPSiteType.GeneratedMaximum;
        this.algoVersion = 3;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final SavedPassword other = (SavedPassword) obj;
        if (this.algoVersion != other.algoVersion) {
            return false;
        }
        if (this.counter != other.counter) {
            return false;
        }
        if (this.mpSiteType != other.mpSiteType) {
            return false;
        }
        if (this.notes == null) {
            if (other.notes != null) {
                return false;
            }
        } else if (!this.notes.equals(other.notes)) {
            return false;
        }
        if (this.website == null) {
            if (other.website != null) {
                return false;
            }
        } else if (!this.website.equals(other.website)) {
            return false;
        }
        return true;
    }

    public int getAlgoVersion() {
        return this.algoVersion;
    }

    public int getCounter() {
        return this.counter;
    }

    public String getNotes() {
        return this.notes;
    }

    public MPSiteType getPasswordType() {
        return this.mpSiteType;
    }

    public String getWebsite() {
        return this.website;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.algoVersion;
        result = prime * result + this.counter;
        result = prime * result + (this.mpSiteType == null ? 0 : this.mpSiteType.hashCode());
        result = prime * result + (this.notes == null ? 0 : this.notes.hashCode());
        result = prime * result + (this.website == null ? 0 : this.website.hashCode());
        return result;
    }

    public void setAlgoVersion(final int algoVersion) {
        this.algoVersion = algoVersion;
    }

    public void setCounter(final Integer counter) {
        this.counter = counter;
    }

    public void setNotes(final String notes) {
        this.notes = notes;
    }

    public void setPasswordType(final MPSiteType passwordType) {
        this.mpSiteType = passwordType;
    }

    public void setWebsite(final String website) {
        this.website = website;
    }
}
