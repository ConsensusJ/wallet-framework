package omnijwallet;

import foundation.omni.CurrencyID;
import foundation.omni.OmniDivisibleValue;
import foundation.omni.rpc.BalanceEntry;
import foundation.omni.rpc.OmniClient;
import io.reactivex.Observable;
import io.reactivex.processors.PublishProcessor;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.TestNet3Params;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@Singleton
public class OmniAdapWalletService {
    private static final Logger log = LoggerFactory.getLogger(OmniAdapWalletService.class);
    private static final URI omniRpcURI = URI.create("http://127.0.0.1:18332");
    private static final String rpcUser = "rpcuser";
    private static final String rpcPassword = "rpcpassword";

    private final NetworkParameters netParams = TestNet3Params.get();
    private LegacyAddress defaultAddress;
    private BalanceEntry omniBalance = new BalanceEntry(OmniDivisibleValue.of(0),OmniDivisibleValue.of(0),OmniDivisibleValue.of(0));

    private final OmniClient omniClient;
    private static final int initialDelay = 1;
    private static final int period = 60;
    private ScheduledExecutorService stpe;
    private ScheduledFuture<?> future;

    private final PublishProcessor<BalanceEntry> balanceProcessor = PublishProcessor.create();


    public OmniAdapWalletService() {
        // TODO: OmniClient should be injected into the constructor
        try {
            omniClient = new OmniClient(netParams, omniRpcURI,rpcUser,rpcPassword);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        defaultAddress = LegacyAddress.fromBase58(netParams, "muuZ2RXkePUsx9Y6cWt3TCSbQyetD6nKak");
        stpe = Executors.newScheduledThreadPool(2);
        startPolling();
    }

    private void startPolling() {
        Runnable task = () -> {
            try {
                BalanceEntry balanceEntry = omniClient.omniGetBalance(defaultAddress, CurrencyID.OMNI);
                omniBalance = balanceEntry;
                balanceProcessor.onNext(balanceEntry);
                log.info("Omni Balance received from remote Omni Core: {}", omniBalance.toString());

            } catch (IOException e) {
                log.error("IOException: {}", e);
                e.printStackTrace();
            }
        };
        future = stpe.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.SECONDS);

    }

    public Observable<BalanceEntry> subscribeBalance() {
        return balanceProcessor.toObservable();
    }

    public LegacyAddress getDefaultAddress() {
        return defaultAddress;
    }

    public BalanceEntry getOmniBalance() {
        return omniBalance;
    }
}
