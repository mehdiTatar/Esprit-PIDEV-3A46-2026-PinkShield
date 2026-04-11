package org.example;

import java.sql.Timestamp;

public class Wishlist {
    private int id;
    private int user_id;
    private int parapharmacie_id;
    private Timestamp added_at;

    public Wishlist() {
    }

    public Wishlist(int user_id, int parapharmacie_id) {
        this.user_id = user_id;
        this.parapharmacie_id = parapharmacie_id;
    }

    public Wishlist(int id, int user_id, int parapharmacie_id, Timestamp added_at) {
        this.id = id;
        this.user_id = user_id;
        this.parapharmacie_id = parapharmacie_id;
        this.added_at = added_at;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getParapharmacie_id() {
        return parapharmacie_id;
    }

    public void setParapharmacie_id(int parapharmacie_id) {
        this.parapharmacie_id = parapharmacie_id;
    }

    public Timestamp getAdded_at() {
        return added_at;
    }

    public void setAdded_at(Timestamp added_at) {
        this.added_at = added_at;
    }

    @Override
    public String toString() {
        return "Wishlist{" +
                "id=" + id +
                ", user_id=" + user_id +
                ", parapharmacie_id=" + parapharmacie_id +
                ", added_at=" + added_at +
                '}';
    }
}

