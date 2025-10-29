package yunrry.flik.batch.job.reader;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import yunrry.flik.batch.domain.SpotImageRecord;
import yunrry.flik.batch.repository.SpotImageRepository;

import java.util.Iterator;
import java.util.List;

public class SpotImageRepositoryItemReader implements ItemStreamReader<SpotImageRecord> {

    private static final String CTX_LAST_ID = "spotImageReader.lastId";
    private final SpotImageRepository repository;
    private final int pageSize;

    private long lastId = 0L;
    private Iterator<SpotImageRecord> currentIt = null;
    private boolean exhausted = false;

    public SpotImageRepositoryItemReader(SpotImageRepository repository, int pageSize) {
        this.repository = repository;
        this.pageSize = pageSize;
    }

    @Override
    public SpotImageRecord read() {
        if (exhausted) return null;

        if (currentIt == null || !currentIt.hasNext()) {
            List<SpotImageRecord> page = repository.findNextWithoutImages(lastId, pageSize);
            if (page.isEmpty()) {
                exhausted = true;
                return null;
            }
            currentIt = page.iterator();
        }

        SpotImageRecord next = currentIt.next();
        lastId = next.getId();
        return next;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        if (executionContext.containsKey(CTX_LAST_ID)) {
            this.lastId = executionContext.getLong(CTX_LAST_ID);
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        executionContext.putLong(CTX_LAST_ID, this.lastId);
    }

    @Override
    public void close() throws ItemStreamException {
        this.currentIt = null;
    }
}