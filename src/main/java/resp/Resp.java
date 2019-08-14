package resp;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javafx.util.Pair;
import lombok.ToString;

/**
 * @creed: Here be dragons !
 * @author: Ezio
 * @Time: 2019-08-14 11:21
 */
@ToString
public class Resp<T> implements Serializable {

    private static final int SUCCESS = 200;
    private static final int ERROR = 500;

    int code;
    String errorHint;
    T data;

    @Override
    public String toString() {
        return "code=" + code + ",errorHint=" + errorHint + ",data=" + data;
    }

    /** 一些基础方法 */

    private Resp() {
    }

    private boolean isSuccess() {
        return code == SUCCESS;
    }

    private String getErrorHint() {
        return errorHint;
    }

    private int getErrorCode() {
        return code;
    }

    private T getData() {
        return data;
    }

    public static Resp<Null> success() {
        Resp<Null> resp = new Resp<Null>();
        resp.code = SUCCESS;
        resp.data = Null.NULL;
        return resp;
    }

    public static <T> Resp<T> success(T data) {
        Resp<T> resp = new Resp<T>();
        resp.code = SUCCESS;
        resp.data = data;
        return resp;
    }

    public static <T> Resp<T> fail(int errorCode, String hint) {
        Resp<T> resp = new Resp<>();
        resp.code = errorCode;
        resp.errorHint = hint;
        return resp;
    }

    public static <T> Resp<T> fail(String hint) {
        Resp<T> resp = new Resp<>();
        resp.code = ERROR;
        resp.errorHint = hint;
        return resp;
    }

    /** TAG:  判断当前层级 */
    public Resp<T> orElse(Resp<T> f) {
        if (this.isSuccess()) {
            return this;
        }
        return f;
    }

    public Resp<T> orElseGet(Supplier<Resp<T>> supplier) {
        if (this.isSuccess()) {
            return this;
        }
        return supplier.get();
    }

    public <T> Resp<T> filter(Predicate<T> predicate, String errorHint) {
        Objects.requireNonNull(predicate);
        if (!this.isSuccess()) {
            return Resp.fail(this.errorHint);
        }
        return predicate.test((T) data) ? Resp.success((T) this.data) : Resp.fail(errorHint);
    }

    public <F> Resp<F> map(Function<T, F> f) {
        if (!this.isSuccess()) {
            return Resp.fail(this.errorHint);
        }
        return Resp.success(f.apply(this.data));
    }

    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {

        if (this.isSuccess()) {
            return this.data;
        }
        throw exceptionSupplier.get();
    }

    /** TAG: NEXT 系列 确认当前是成功的 */

    public <F> Resp<F> next(Function<T, Resp<F>> f) {
        if (!this.isSuccess()) {
            return Resp.fail(this.errorHint);
        }
        return f.apply(this.data);
    }

    public <F> Resp<F> nextIf(Function<T, Resp<F>> f, F defaultValue) {

        if (!this.isSuccess()) {
            return Resp.fail(this.errorHint);
        }
        Resp<F> resp = f.apply(this.data);
        if (!resp.isSuccess()) {
            return Resp.success(defaultValue);
        }
        return resp;
    }

    public <F> Resp<F> nextIf(Function<T, Resp<F>> f, Supplier<F> defaultValueSupplier) {
        if (!this.isSuccess()) {
            return Resp.fail(this.errorHint);
        }
        Resp<F> resp = f.apply(this.data);
        if (!resp.isSuccess()) {
            return Resp.success(defaultValueSupplier.get());
        }
        return resp;
    }

    public <F> Resp<F> nextIf(boolean condition, Resp<F> defaultResp, String hint) {
        if (!this.isSuccess()) {
            return Resp.fail(this.errorHint);
        }
        if (condition) {
            return defaultResp;
        }
        return Resp.fail(hint);
    }

    public <F> Resp<F> nextIf(boolean condition, Supplier<Resp<F>> defaultRespSupplier, String hint) {
        if (!this.isSuccess()) {
            return Resp.fail(this.errorHint);
        }
        if (condition) {
            return defaultRespSupplier.get();
        }
        return Resp.fail(hint);
    }

    public <F> Resp<F> nextIfElse(boolean condition, Resp<F> resp1, Resp<F> resp2) {
        if (!this.isSuccess()) {
            return Resp.fail(this.errorHint);
        }
        if (condition) {
            return resp1;
        }
        return resp2;
    }

    public <F> Resp<F> nextIfElse(boolean condition, Supplier<Resp<F>> supplier1, Supplier<Resp<F>> supplier2) {
        if (!this.isSuccess()) {
            return Resp.fail(this.errorHint);
        }

        if (condition) {
            return supplier1.get();
        }
        return supplier2.get();
    }

    public <F> Resp<F> nextIfElse(boolean condition, Supplier<Resp<F>> supplier1, Resp<F> resp2) {
        if (!this.isSuccess()) {
            return Resp.fail(this.errorHint);
        }

        if (condition) {
            return supplier1.get();
        }
        return resp2;
    }

    public <F> Resp<F> nextIfElse(boolean condition, Resp<F> resp1, Supplier<Resp<F>> supplier2) {
        if (!this.isSuccess()) {
            return Resp.fail(this.errorHint);
        }

        if (condition) {
            return resp1;
        }
        return supplier2.get();
    }

    public <F> Resp<F> nextIfElse(Optional optional, Resp<F> resp1, Resp<F> resp2) {
        if (!this.isSuccess()) {
            return Resp.fail(this.errorHint);
        }

        if (optional.isPresent()) {
            return resp1;
        }
        return resp2;
    }

    public <F> Resp<F> nextIfElse(Optional optional, Supplier<Resp<F>> supplier1, Supplier<Resp<F>> supplier2) {
        if (!this.isSuccess()) {
            return Resp.fail(this.errorHint);
        }

        if (optional.isPresent()) {
            return supplier1.get();
        }
        return supplier2.get();
    }

    public <F> Resp<F> nextIfElse(Optional optional, Supplier<Resp<F>> supplier1, Resp<F> resp2) {
        if (!this.isSuccess()) {
            return Resp.fail(this.errorHint);
        }
        if (optional.isPresent()) {
            return supplier1.get();
        }
        return resp2;
    }

    public <F> Resp<F> nextIfElse(Optional optional, Resp<F> resp1, Supplier<Resp<F>> supplier2) {
        if (!this.isSuccess()) {
            return Resp.fail(this.errorHint);
        }
        if (optional.isPresent()) {
            return resp1;
        }
        return supplier2.get();
    }

    public <F> Resp<F> nextOptional(Function<T, Optional<F>> f, String errorHint) {
        if (!this.isSuccess()) {
            return Resp.fail(this.errorHint);
        }

        Optional<F> apply = f.apply(this.data);
        return assertion(apply.isPresent(), apply::get, errorHint);
    }

    public <F> Resp<F> nextOptional(Function<T, Optional<F>> f, Supplier<String> errorHintSupplier) {
        if (!this.isSuccess()) {
            return Resp.fail(this.errorHint);
        }

        Optional<F> apply = f.apply(this.data);
        return assertion(apply.isPresent(), apply::get, errorHintSupplier);
    }



    @SafeVarargs
    public final <F> Resp<F> choose(String errorHint, Pair<Predicate<T>, Supplier<Resp<F>>>... pairs) {
        if (!this.isSuccess()) {
            return Resp.fail(this.errorHint);
        }

        AtomicReference<Resp<F>> resp = new AtomicReference<>(Resp.fail(errorHint));
        AtomicBoolean isBreak = new AtomicBoolean(false);
        for (Pair<Predicate<T>, Supplier<Resp<F>>> pair : pairs) {
            if (!isBreak.get() && pair.getKey().test(data)) {
                resp.set(pair.getValue().get());
                isBreak.set(true);
            }
        }
        return resp.get();
    }

    public <F> Resp<F> choose(String errorHint, Map<Predicate<T>, Supplier<Resp<F>>> resps) {

        if (!this.isSuccess()) {
            return Resp.fail(this.errorHint);
        }

        AtomicReference<Resp<F>> resp = new AtomicReference<>(Resp.fail(errorHint));
        AtomicBoolean isBreak = new AtomicBoolean(false);

        resps.forEach((predicate, respSupplier) -> {
            if (!isBreak.get() && predicate.test(data)) {
                resp.set(respSupplier.get());
                isBreak.set(true);
            }
        });
        return resp.get();
    }


    public static <T> Resp<T> assertion(Optional<T> optional, String errorHint) {

        return optional.isPresent() ? Resp.success(optional.get()) : Resp.fail(errorHint);
    }

    public static <T> Resp<T> assertion(Optional<T> optional, int errorCode, String errorHint) {

        return optional.isPresent() ? Resp.success(optional.get()) : Resp.fail(errorCode, errorHint);
    }

    public static <T> Resp<T> assertion(Optional<T> optional, Supplier<String> errorHintSupplier) {

        return optional.isPresent() ? Resp.success(optional.get()) : Resp.fail(errorHintSupplier.get());
    }

    public static <T> Resp<T> assertion(Optional<T> optional, int errorCode, Supplier<String> errorHintSupplier) {

        return optional.isPresent() ? Resp.success(optional.get()) : Resp.fail(errorCode, errorHintSupplier.get());
    }

    public static <T> Resp<T> assertion(boolean success, Supplier<T> dataSupplier, String errorHint) {

        return success ? Resp.success(dataSupplier.get()) : Resp.fail(errorHint);
    }

    public static <T> Resp<T> assertion(boolean success, Supplier<T> dataSupplier, int errorCode, String errorHint) {

        return success ? Resp.success(dataSupplier.get()) : Resp.fail(errorCode, errorHint);
    }

    public static <T> Resp<T> assertion(boolean success, Supplier<T> dataSupplier, Supplier<String> errorHintSupplier) {

        return success ? Resp.success(dataSupplier.get()) : Resp.fail(errorHintSupplier.get());
    }

    public static <T> Resp<T> assertion(boolean success, Supplier<T> dataSupplier, int errorCode, Supplier<String> errorHintSupplier) {

        return success ? Resp.success(dataSupplier.get()) : Resp.fail(errorCode, errorHintSupplier.get());
    }

    public static <T> Resp<T> assertion(boolean success, T data, String errorHint) {

        return success ? Resp.success(data) : Resp.fail(errorHint);
    }

    public static <T> Resp<T> assertion(boolean success, T data, int errorCode, String errorHint) {

        return success ? Resp.success(data) : Resp.fail(errorCode, errorHint);
    }

    public static <T> Resp<T> assertion(boolean success, T data, Supplier<String> errorHintSupplier) {

        return success ? Resp.success(data) : Resp.fail(errorHintSupplier.get());
    }

    public static <T> Resp<T> assertion(boolean success, T data, int errorCode, Supplier<String> errorHintSupplier) {

        return success ? Resp.success(data) : Resp.fail(errorCode, errorHintSupplier.get());
    }
}
