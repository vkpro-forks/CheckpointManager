package ru.ac.checkpointmanager.extension.pass;

import org.junit.jupiter.api.extension.ExtensionContext;
import ru.ac.checkpointmanager.repository.VisitorRepository;

public abstract class CarAndVisitorPassTestPreparationRepositoryInjector extends CarPassTestPreparationRepositoryInjector {

    protected VisitorRepository visitorRepository;

    @Override
    protected void initRepos(ExtensionContext context) {
        super.initRepos(context);
        visitorRepository = applicationContext.getBean(VisitorRepository.class);
    }
}
