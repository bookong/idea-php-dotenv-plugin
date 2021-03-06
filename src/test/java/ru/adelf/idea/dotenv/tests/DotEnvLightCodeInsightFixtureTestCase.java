package ru.adelf.idea.dotenv.tests;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.util.indexing.FileBasedIndexImpl;
import com.intellij.util.indexing.ID;
import org.jetbrains.annotations.NotNull;
import ru.adelf.idea.dotenv.api.EnvironmentVariablesApi;
import ru.adelf.idea.dotenv.indexing.DotEnvKeyValuesIndex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Copy of LaravelLightCodeInsightFixtureTestCase from laravel plugin
 */
public abstract class DotEnvLightCodeInsightFixtureTestCase extends BasePlatformTestCase {

    protected String basePath = "src/test/java/ru/adelf/idea/dotenv/tests/";

    protected void assertIndexContains(@NotNull ID<String, ?> id, @NotNull String... keys) {
        assertIndex(id, false, keys);
    }

    protected void assertIndexNotContains(@NotNull ID<String, ?> id, @NotNull String... keys) {
        assertIndex(id, true, keys);
    }

    private void assertIndex(@NotNull ID<String, ?> id, boolean notCondition, @NotNull String... keys) {
        for (String key : keys) {
            final Collection<VirtualFile> virtualFiles = new ArrayList<>();

            FileBasedIndexImpl.getInstance().getFilesWithKey(id, new HashSet<>(Collections.singletonList(key)), virtualFile -> {
                virtualFiles.add(virtualFile);
                return true;
            }, GlobalSearchScope.allScope(getProject()));

            if (notCondition && virtualFiles.size() > 0) {
                fail(String.format("Fail that ID '%s' not contains '%s'", id.toString(), key));
            } else if (!notCondition && virtualFiles.size() == 0) {
                fail(String.format("Fail that ID '%s' contains '%s'", id.toString(), key));
            }
        }
    }

    protected void assertUsagesContains(@NotNull String... keys) {
        for (String key : keys) {
            PsiElement[] usages = EnvironmentVariablesApi.getKeyUsages(this.myFixture.getProject(), key);
            if (usages.length == 0) {
                fail(String.format("Fail that usages contains '%s'", key));
            }
        }
    }

    protected void assertContainsKeyAndValue(@NotNull String key, @NotNull String value) {
        assertIndexContains(DotEnvKeyValuesIndex.KEY, key);

        final AtomicBoolean found = new AtomicBoolean(false);

        FileBasedIndexImpl.getInstance().processValues(DotEnvKeyValuesIndex.KEY, key, null, (virtualFile, s) -> {
            if (s.equals(value)) {
                found.set(true);
            }
            return false;
        }, GlobalSearchScope.allScope(myFixture.getProject()));

        if (!found.get()) {
            fail(String.format("Fail that index contains pair '%s' => '%s'", key, value));
        }
    }
}
