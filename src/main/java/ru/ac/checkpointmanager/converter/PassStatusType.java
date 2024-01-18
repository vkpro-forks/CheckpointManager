package ru.ac.checkpointmanager.converter;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.SqlTypes;
import org.hibernate.usertype.UserType;
import ru.ac.checkpointmanager.model.passes.PassStatus;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

@Slf4j
public class PassStatusType implements UserType<PassStatus> {

    public static final PassStatusType INSTANCE = new PassStatusType();


    @Override
    public int getSqlType() {
        return SqlTypes.OTHER;
    }

    @Override
    public Class<PassStatus> returnedClass() {
        return PassStatus.class;
    }

    @Override
    public boolean equals(PassStatus x, PassStatus y) {
        return x.name().equals(y.name());
    }

    @Override
    public int hashCode(PassStatus x) {
        return Objects.hashCode(x);
    }

    @Override
    public PassStatus nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner)
            throws SQLException {
        String columnValue = (String) rs.getObject(position);
        if (rs.wasNull()) {
            columnValue = null;
        }
        log.debug("Result set column {} value is {}", position, columnValue);
        return PassStatus.valueOf(columnValue);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, PassStatus value, int index, SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            log.debug("Binding null to parameter {} ", index);
            st.setNull(index, Types.OTHER);
        } else {
            log.debug("binding parameter [{}] as [PassStatus] - [{}] ", value.name(), index);
            st.setObject(index, value, Types.OTHER);
        }
    }

    @Override
    public PassStatus deepCopy(PassStatus value) {
        return value == null ? null :
                PassStatus.valueOf(value.name());
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(PassStatus value) {
        return deepCopy(value);
    }

    @Override
    public PassStatus assemble(Serializable cached, Object owner) {
        return deepCopy((PassStatus) cached);
    }

}
