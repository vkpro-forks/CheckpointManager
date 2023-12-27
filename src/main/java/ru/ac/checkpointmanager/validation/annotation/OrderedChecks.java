package ru.ac.checkpointmanager.validation.annotation;

import jakarta.validation.GroupSequence;
import jakarta.validation.groups.Default;

@GroupSequence({Default.class, CustomCheck.class})
public interface OrderedChecks {
}
