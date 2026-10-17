
        package com.example.cricketscorer.models;

import java.io.Serializable;

public class Team implements Serializable {

    private long id;

    private String name;
    private String shortName;
    private String iconUri;

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    public Team() {
        name = "";
        shortName = "";
        iconUri = "";
    }

    public Team(
            String name,
            String shortName
    ) {

        this();

        setName(name);
        setShortName(shortName);
    }

    public Team(
            long id,
            String name,
            String shortName
    ) {

        this(
                name,
                shortName
        );

        this.id = id;
    }

    // ============================================================
    // ID
    // ============================================================

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    // ============================================================
    // TEAM NAME
    // ============================================================

    public String getName() {
        return name;
    }

    public void setName(String name) {

        this.name =
                name == null
                        ? ""
                        : name.trim();
    }

    // ============================================================
    // SHORT NAME
    // ============================================================

    public String getShortName() {
        return shortName;
    }

    public void setShortName(
            String shortName
    ) {

        this.shortName =
                shortName == null
                        ? ""
                        : shortName.trim();
    }

    // ============================================================
    // ICON URI
    // ============================================================

    public String getIconUri() {
        return iconUri;
    }

    public void setIconUri(
            String iconUri
    ) {

        this.iconUri =
                iconUri == null
                        ? ""
                        : iconUri;
    }

    // ============================================================
    // DISPLAY NAME
    // ============================================================

    public String getDisplayName() {

        if (name != null
                && !name.trim().isEmpty()) {

            return name;
        }

        if (shortName != null
                && !shortName.trim().isEmpty()) {

            return shortName;
        }

        return "Unnamed Team";
    }

    // ============================================================
    // DISPLAY SHORT NAME
    // ============================================================

    public String getDisplayShortName() {

        if (shortName != null
                && !shortName.trim().isEmpty()) {

            return shortName.toUpperCase();
        }

        if (name != null
                && !name.trim().isEmpty()) {

            String cleanName =
                    name.trim();

            if (cleanName.length() <= 5) {
                return cleanName.toUpperCase();
            }

            return cleanName
                    .substring(0, 5)
                    .toUpperCase();
        }

        return "--";
    }

    // ============================================================
    // VALIDATION
    // ============================================================

    public boolean isValid() {

        return name != null
                && !name.trim().isEmpty()
                && shortName != null
                && !shortName.trim().isEmpty();
    }

    // ============================================================
    // EQUALITY
    // ============================================================

    @Override
    public boolean equals(
            Object obj
    ) {

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Team)) {
            return false;
        }

        Team other =
                (Team) obj;

        /*
         * Database IDs are the reliable identity once
         * a team has been stored.
         */
        if (id > 0 && other.id > 0) {
            return id == other.id;
        }

        return false;
    }

    @Override
    public int hashCode() {

        if (id > 0) {
            return Long.valueOf(id)
                    .hashCode();
        }

        return 0;
    }

    // ============================================================
    // STRING REPRESENTATION
    // ============================================================

    @Override
    public String toString() {

        return getDisplayName();
    }
}

