package com.ata.lambdaexpressions.classroom;

import com.ata.lambdaexpressions.classroom.converter.RecipeConverter;
import com.ata.lambdaexpressions.classroom.dao.CartonDao;
import com.ata.lambdaexpressions.classroom.dao.RecipeDao;
import com.ata.lambdaexpressions.classroom.exception.CartonCreationFailedException;
import com.ata.lambdaexpressions.classroom.exception.RecipeNotFoundException;
import com.ata.lambdaexpressions.classroom.model.Carton;
import com.ata.lambdaexpressions.classroom.model.Ingredient;
import com.ata.lambdaexpressions.classroom.model.Recipe;
import com.ata.lambdaexpressions.classroom.model.Sundae;

import com.google.common.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;

import static java.util.stream.Nodes.collect;

/**
 * Provides Ice Cream Parlor functionality.
 */
public class IceCreamParlorService {
    private final RecipeDao recipeDao;
    private final CartonDao cartonDao;
    private final IceCreamMaker iceCreamMaker;

    /**
     * Constructs service with the provided DAOs.
     * @param recipeDao the RecipeDao to use for accessing recipes
     * @param cartonDao the CartonDao to use for accessing ice cream cartons
     */
    @Inject
    public IceCreamParlorService(RecipeDao recipeDao, CartonDao cartonDao, IceCreamMaker iceCreamMaker) {
        this.recipeDao = recipeDao;
        this.cartonDao = cartonDao;
        this.iceCreamMaker = iceCreamMaker;
    }

    /**
     * Creates and returns the sundae defined by the given ice cream flavors.
     * If a flavor is not found or we have none of that flavor left, the sundae
     * is returned, but without that flavor. (We'll only charge the customer for
     * the scoops they are returned)
     * @param flavorNames List of flavors to include in the sundae
     * @return The newly created Sundae
     */
    public Sundae getSundae(List<String> flavorNames) {
        // This does the filtering out of any unknown flavors, so only
        // Cartons of known flavors will be returned.
        List<Carton> cartons = cartonDao.getCartonsByFlavorNames(flavorNames);

        // PHASE 1: Use removeIf() to remove any empty cartons from cartons
        /*cartons.removeIf((carton) -> carton.isEmpty());*/
/*
        cartons.removeIf(carton -> carton.isEmpty());
*/
        cartons.removeIf(Carton::isEmpty);// Method reference for removeIf()







        //Pass each carton to a lambda expression to see if its empty






        return buildSundae(cartons);
    }

    @VisibleForTesting
    Sundae buildSundae(List<Carton> cartons) {
        Sundae sundae = new Sundae();

        // PHASE 2: Use forEach() to add one scoop of each flavor
        // remaining in cartons

        cartons.forEach((aCarton) -> {sundae.addScoop(aCarton.getFlavor());});
        
        //Go through a list oCartons we and add one scoop to the sundae choosing the flavor
        //Since cartons is a list we can use a list forEach instead of a Stream


        return sundae;
    }

    /**
     * Prepares the specified flavors, creating 1 carton of each provided
     * flavor.
     *
     * A flavor name that doesn't correspond
     * to a known recipe will result in CartonCreationFailedException, and
     * no Cartons will be created.
     *
     * @param flavorNames List of names of flavors to create new batches of
     * @return the number of cartons produced by the ice cream maker
     */
    public int prepareFlavors(List<String> flavorNames) {
        // this is a map operation to convert List<String> to List<Recipe>
        // not a stream map
        List<Recipe> recipes = map(
            flavorNames,//we call inputin the helper method
            (flavorName) -> {we call converter inthe helper
                // trap the checked exception, RecipeNotFoundException, and
                // wrap in a runtime exception because our lambda can't throw
                // checked exceptions
                try {
                    return recipeDao.getRecipe(flavorName);
                } catch (RecipeNotFoundException e) {
                    throw new CartonCreationFailedException("Could not find recipe for " + flavorName, e);
                }
            }
        );

        // PHASE 3: Replace right hand side: use map() to convert List<Recipe> to List<Queue<Ingredient>>
        /*List<Queue<Ingredient>> ingredientQueues = new ArrayList<>();
        * All what we are doing in the Lambda expression is calling a static method in a class we can
        *  use a method
        * in the Lambda express*/

         /*List<Queue<Ingredient>> ingredientQueues = recipes.stream()
                 .map(RecipeConverter::fromRecipeToIngredientQueue)
                 .collect(Collectors.toList());*/

        List<Queue<Ingredient>> ingredientQueues = recipes.stream()
                .map(aRecipe) -> RecipeConverter.fromRecipeToIngredientQueue(aRecipe)
                .collect(Collectors.toList());
         return makeIceCreamCartons(ingredientQueues);
    }

    @VisibleForTesting
    int makeIceCreamCartons(List<Queue<Ingredient>> ingredientQueues) {
        // don't change any of the lines that touch cartonsCreated.
        int cartonsCreated = 0;
        for (Queue<Ingredient> ingredients : ingredientQueues) {

            // PHASE 4: provide Supplier to prepareIceCream()
            //Use a Lambda expression to get out ingredients from the Queue ingrdients
            // and pass it to iceCreamMaker.prepareIceCreamCarton()
            // If iceCreamMaker.prepareIceCreamCarton() returns true, increment cartonsCreated by 1.
            if (iceCreamMaker.prepareIceCreamCarton(() -> ingredients.poll())) {
                cartonsCreated++;
            }
        }

        return cartonsCreated;
    }

    /**
     * Converts input list of type T to a List of type R, where each entry in the return
     * value is the output of converter applied to each entry in input.
     *
     * (We will get to Java streams in a later lesson, at which point we won't need a helper method
     * like this.)
     * 
     * @Param List
     * @Param Functional Reference name a method or Lambda expression
     * <T, R> means 2 generics data will be referenced
     * List<T> means a List of the first generic data
     * List<R> means a list of the second generic data
     * function<T, R> means a method that have 2 parameters
     *  FUnction converter receive two parameters 1 and 2nd types
     *  List<R> RETURN A 2ND TYPE AND RECEIVE A List<T> of 1st type</T> 
     */
    private <T, R> List<R> map(List<T> input, Function<T, R> converter) {
        //Use the stram interfce map() method to dun the converter method (second parameter)
        //passed to it
        return input.stream()
            .map(converter)// Pass the function we received to the stram map
            .collect(Collectors.toList());
    }
}
