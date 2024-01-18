package ru.ac.checkpointmanager.model.usertype;

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

/**
 * Класс для связывания enum {@link PassStatus} с кастомным Postgres TYPE pass_status_enum
 */
@Slf4j
public class PassStatusType implements UserType<PassStatus> {

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

    /**
     * Связывает тип енама в PG с полем PassStatus объекта Pass
     *
     * @throws SQLException если произойдет несоответствие типов - будет ERROR
     */
    @Override
    public PassStatus nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner)
            throws SQLException {
        String columnValue = (String) rs.getObject(position);
        if (rs.wasNull()) {
            log.warn("Set null parameter of PassStatus to Pass");
            columnValue = null;
        }
        log.debug("Result set column {} value is {}", position, columnValue);
        return PassStatus.valueOf(columnValue);
    }

    /**
     * Связывает поле PassStatus с типом енама в PG
     *
     * @throws SQLException если произойдет несоответствие типов - будет ERROR
     */
    @Override
    public void nullSafeSet(PreparedStatement st, PassStatus value, int index, SharedSessionContractImplementor session)
            throws SQLException {
        if (value == null) {
            log.warn("Binding null to parameter [index: {}]", index);
            st.setNull(index, Types.OTHER);
        } else {
            log.debug("Binding parameter [{}] as [PassStatus] - [index: {}] of Pass", value.name(), index);
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
