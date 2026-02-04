package com.mylinehub.crm.rag.util;

import com.pgvector.PGvector;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.*;

public class PGVectorType implements UserType {

    @Override
    public int[] sqlTypes() {
        //System.out.println("[PGVectorType] sqlTypes called");
        return new int[]{Types.OTHER}; // PostgreSQL 'vector' type
    }

    @Override
    public Class<?> returnedClass() {
        //System.out.println("[PGVectorType] returnedClass called");
        return PGvector.class;
    }

    @Override
    public boolean equals(Object x, Object y) {
        //System.out.println("[PGVectorType] equals called: x=" + x + ", y=" + y);
        if (x == y) return true;
        if (x == null || y == null) return false;
        return x.equals(y);
    }

    @Override
    public int hashCode(Object x) {
        //System.out.println("[PGVectorType] hashCode called: x=" + x);
        return x.hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names,
                              SharedSessionContractImplementor session, Object owner) throws SQLException {
        //System.out.println("[PGVectorType] nullSafeGet called for column: " + names[0]);
        Object obj = rs.getObject(names[0]);
        //System.out.println("[PGVectorType] Value from ResultSet: " + obj);
        if (obj == null) return null;
        if (obj instanceof PGvector) return obj;
        PGvector vector = new PGvector(obj.toString());
        //System.out.println("[PGVectorType] Parsed PGvector: " + vector);
        return vector;
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index,
                            SharedSessionContractImplementor session) throws SQLException {
        //System.out.println("[PGVectorType] nullSafeSet called for index: " + index + ", value: " + value);
        if (value == null) {
            st.setNull(index, Types.OTHER);
            //System.out.println("[PGVectorType] Set NULL for index " + index);
        } else {
            PGvector vector = (PGvector) value;
            st.setObject(index, vector, Types.OTHER);
            //System.out.println("[PGVectorType] Set PGvector for index " + index + ": " + vector);
        }
    }

    @Override
    public Object deepCopy(Object value) {
        //System.out.println("[PGVectorType] deepCopy called for value: " + value);
        if (value == null) return null;
        PGvector v = (PGvector) value;
        try {
            PGvector copy = new PGvector(v.toString());
            //System.out.println("[PGVectorType] deepCopy result: " + copy);
            return copy;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean isMutable() {
        //System.out.println("[PGVectorType] isMutable called");
        return true;
    }

    @Override
    public Serializable disassemble(Object value) {
        //System.out.println("[PGVectorType] disassemble called for value: " + value);
        return (Serializable) deepCopy(value);
    }

    @Override
    public Object assemble(Serializable cached, Object owner) {
        //System.out.println("[PGVectorType] assemble called for cached: " + cached);
        return deepCopy(cached);
    }

    @Override
    public Object replace(Object original, Object target, Object owner) {
        //System.out.println("[PGVectorType] replace called, original: " + original + ", target: " + target);
        return deepCopy(original);
    }
}
