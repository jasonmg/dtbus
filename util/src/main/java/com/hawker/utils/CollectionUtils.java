package com.hawker.utils;

import com.hawker.utils.exception.ExceptionHandler;
import com.hawker.utils.exception.ExceptionHandlerDefault;
import fj.*;
import fj.data.Validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static fj.Monoid.monoidDef;
import static java.util.function.Function.identity;

public final class CollectionUtils {

    @SuppressWarnings("unchecked")
    public static <T> fj.data.List<T> toFJList(final java.util.List<T> javaList) {
        return fj.data.List.arrayList((T[]) javaList.toArray());
    }

//    /**
//     * from [[1,2],[3,4],[5,6]] to [1,2,3,4,5,6]
//     */
//    public static <T> List<T> flattenList(final List<List<T>> list) {
//        Monoid<List<T>> m = javaListMonoid();
//        return toFJList(list).foldLeft(m.sum(), m.zero());
//    }

    /**
     * from [[1,2],[3,4],[5,6]] to [1,2,3,4,5,6]
     * use fj.data.List.join
     */
    public static <T> List<T> flattenList(final List<List<T>> list) {
        fj.data.List<fj.data.List<T>> fjLists = toFJList(
                list.stream()
                        .map(CollectionUtils::toFJList)
                        .collect(Collectors.toList()));

        return fj.data.List.join(fjLists).toJavaList();
    }

    public static <A, B, C> P3<List<A>, List<B>, List<C>> unzip3(final List<P3<A, B, C>> listOfP3) {

        List<A> listA = new ArrayList<>();
        List<B> listB = new ArrayList<>();
        List<C> listC = new ArrayList<>();

        listOfP3.forEach(p3 -> {
            listA.add(p3._1());
            listB.add(p3._2());
            listC.add(p3._3());
        });

        return P.p(listA, listB, listC);
    }

    public static <T> Optional<List<T>> flip(final List<Optional<T>> list) {
        return traverseOption(list, identity());
    }


    /**
     * @param list list of something
     * @param f    function applied against every list element
     * @return optional contain the first element match the condition otherwise return empty()
     */
    public static <T> Optional<T> findFirst(final List<T> list, Function<T, Boolean> f) {
        return list.stream()
                .filter(f::apply)
                .findFirst();
    }


    // ==========  java list high order function ============= //


    public static <E> P2<List<E>, List<E>> batchApplyResultWithOutE(List<E> inputs,
                                                                    Consumer<E> f,
                                                                    Optional<ExceptionHandler<E>> handler) {

        ExceptionHandler<E> _handler = handler.orElseGet(ExceptionHandlerDefault::new);

        P2<List<E>, List<E>> p2 = batchApplyResult(inputs, f).map1(
                listOfE -> listOfE.stream()
                        .peek(p -> _handler.handle(p._2(), p._1()))
                        .map(P2::_2)
                        .collect(Collectors.toList())

        );

        return P.p(p2._1(), p2._2());
    }

    /**
     * 批量执行函数方法,返回 P2
     * P2._1() list of 失败的 id 和 exception
     * P2._2() list of 成功的 id
     */
    public static <E> P2<List<P2<Exception, E>>, List<E>> batchApplyResult(List<E> inputs, Consumer<E> f) {

        fj.data.List<Validation<P2<Exception, E>, E>> res = toFJList(inputs)
                .map(i -> Try.<E, Exception>f(() -> {
                    f.accept(i);
                    return i;
                }).f().f().map(e -> P.p(e, i)));

        return res.partition(Validation::isFail).split(
                listOfError ->
                        listOfError.map(v -> v.fail()).toJavaList()
                ,
                listOfValue ->
                        listOfValue.map(v -> v.success()).toJavaList()
        );
    }


    public static <A> Monoid<java.util.List<A>> javaListMonoid() {
        return monoidDef(new Monoid.Definition<java.util.List<A>>() {
            @Override
            public List<A> empty() {
                return new ArrayList<>();
            }

            @Override
            public List<A> append(List<A> a1, List<A> a2) {
                a1.addAll(a2);
                return a1;
            }
        });
    }

    public static <A, B> Optional<List<B>> traverseOption(final List<A> list, Function<A, Optional<B>> f) {
        return toFJList(list).foldRight(
                (a, obs) -> f.apply(a).map(o ->
                        obs.map(os -> {
                            os.add(o);
                            return os;
                        }).get()
                ),
                Optional.of(new ArrayList<>()));
    }

}
