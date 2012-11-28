/*
 * Copyright (c) 2012 The 99 Software Foundation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.nnsoft.be3.model.nested;

import org.nnsoft.be3.annotations.RDFClassType;
import org.nnsoft.be3.annotations.RDFIdentifier;

/**
 * @author Matteo Moci ( matteo.moci (at) gmail.com )
 */
@RDFClassType(type = "http://collective.com/Author")
public class Author {

    private int id;

    public Author() {}

    @RDFIdentifier
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Author(int id) {
        this.id = id;
    }

    public static class AuthorBuilder {

        private Author author = new Author();

        public static AuthorBuilder author() {
            return new AuthorBuilder();
        }

        public AuthorBuilder withId(int id) {
            author.setId(id);
            return this;
        }

        public Author build() {
            validate();
            return author;
        }

        private void validate() {
            //validate stuff
        }
    }
}
