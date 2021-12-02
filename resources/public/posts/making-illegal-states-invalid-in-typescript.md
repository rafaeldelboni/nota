# Making illegal states invalid in typescript

There is a great series about "Designing with types" from Scott Wlaschin
[here](https://fsharpforfunandprofit.com/posts/designing-with-types-intro/).
One particular post that caught my attention and made me think for a little while was 
["Designing with types: Making illegal states unrepresentable"](https://fsharpforfunandprofit.com/posts/designing-with-types-making-illegal-states-unrepresentable/), where the author explores a somewhat "unconventional" type definition.  

As I find myself referring a lot to this concept, I thought it would be nice to write down a direct translation
to the typescript world.  
Let's first establish our problem *(a simple todo list)*:

* A task can be created with a description
* Once a task is marked as done, one can always check out at which point in time

The most simple, naive type that represents the domain problem would probably look something like this:
```typescript
type Todo = {
  description: string
  done: boolean
  doneAt: Date | null
}

// × completed task, without a date 
const task1: Todo = {
  description: "#task 1",
  done: true,
  doneAt: null
}

// × pending task, with a date filled in 
const task2: Todo = {
  description: "#task 2",
  done: false,
  doneAt: new Date()
}
```

The issue here is the type system allowing the developer to create illegal states.
To solve that issue, a validation layer could be created, but that would require more code. 
The type also lacks clarity, to the reader, it might not be immediately clear that *doneAt* should only
be filled in when the field *done* is `true`.  

Applying the concept mentioned above, a better type system would look something like this:
```typescript
type PendingTodo = {
  description: string;
  done: false
}
type CompletedTodo = {
  description: string;
  done: true;
  doneAt: Date;
}
type Todo = PendingTodo | CompletedTodo
```

The advantages are:
  * Defining task1 and task2 like before would break the compilation.
  * The code relying on the types can be more expressive in terms of which is the expected state of the task.

```typescript
function complete(aTodo: PendingTodo) {
  // ...
}

const todo: Todo = {
  description: 'feed the dog',
  done: false
}
complete(todo) // × compilation error
if (!todo.done) complete(todo) // × compilation success 
```

Because types only exist at compilation time, if you are creating a typed variable from
input external to your code *(e.g.: http request, form input, etc...)*, a validation check is still required.
Next time, I'd like to explore a more seamlessly validation layer for these cases.
