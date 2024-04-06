package ru.ac.checkpointmanager.model.usertype;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.SqlTypes;
import org.hibernate.usertype.UserType;
import ru.ac.checkpointmanager.model.payment.CurrencyEnum;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

@Slf4j
public class CurrencyType implements UserType<CurrencyEnum> {
    @Override
    public int getSqlType() {
        return SqlTypes.OTHER;
    }

    @Override
    public Class<CurrencyEnum> returnedClass() {
        return CurrencyEnum.class;
    }

    @Override
    public boolean equals(CurrencyEnum x, CurrencyEnum y) {
        return x.name().equals(y.name());
    }

    @Override
    public int hashCode(CurrencyEnum x) {
        return Objects.hashCode(x);
    }

    /**
     * Связывает значение енама CurrencyType, со енамом Java CurrencyEnum
     */
    @Override
    public CurrencyEnum nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner) throws SQLException {
        String columnValue = (String) rs.getObject(position);
        if (rs.wasNull()) {
            log.warn("Set null parameter of CurrencyType to Donation");
            columnValue = null;
        }
        log.debug("{}: column {} value is {}", this.getClass().getSimpleName(), position, columnValue);
        return CurrencyEnum.valueOf(columnValue);
    }

    /**
     * Связывает поле CurrencyEnum с типом ПГ CurrencyType
     */
    @Override
    public void nullSafeSet(PreparedStatement st, CurrencyEnum value, int index, SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            log.warn("Binding null to parameter [index: {}]", index);
            st.setNull(index, Types.OTHER);
        } else {
            log.debug("Binding parameter [{}] as [CyrrencyType] - [index: {}] of Donation", value.name(), index);
            st.setObject(index, value, Types.OTHER);
        }
    }

    @Override
    public CurrencyEnum deepCopy(CurrencyEnum value) {
        return value == null ? null :
                CurrencyEnum.valueOf(value.name());
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(CurrencyEnum value) {
        return deepCopy(value);
    }

    @Override
    public CurrencyEnum assemble(Serializable cached, Object owner) {
        return deepCopy((CurrencyEnum) cached);
    }
}
